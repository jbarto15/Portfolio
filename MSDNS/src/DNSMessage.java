import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {
    private DNSHeader header_;
    private ArrayList<DNSQuestion> questions_ = new ArrayList<>();
    private ArrayList<DNSRecord> answers_ = new ArrayList<>();
    private ArrayList<DNSRecord> records_ = new ArrayList<>();
    private ArrayList<DNSRecord> additionalRecords_ = new ArrayList<>();
    private byte[] completeMessage_;


    /**
     * This method decodes the data from the packet that was sent to the server from the client
     *
     * @param bytes - a byte array that contains all the DNS request information
     * @return - returns a DNSMessage object
     */
    public static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        // Create message object
        DNSMessage message = new DNSMessage();

        // Store the complete message to handle the compression technique
        message.completeMessage_ = Arrays.copyOf(bytes, bytes.length);

        // Store the header information
        InputStream input = new ByteArrayInputStream(bytes);
        message.header_ = DNSHeader.decodeHeader(input);

        // Loop through the number of questions in the request and store all the questions
        for ( int i = 0; i < message.header_.getQuestionCount_(); i++ ) {
            message.questions_.add(DNSQuestion.decodeQuestion(input, message));
        }

        // Loop through the number of answers in the request and store all the records
        for ( int i = 0; i < message.header_.getAnswerCount_(); i++ ) {
            message.answers_.add(DNSRecord.decodeRecord(input, message));
        }

        // Loop through the number of records in the request and store all the records
        for ( int i = 0; i < message.header_.getAuthorityRecordCount(); i++ ) {
            message.records_.add(DNSRecord.decodeRecord(input, message));
        }

        // Loop through the number of records in the request and store all the records
        for ( int i = 0; i < message.header_.getAdditionalRecordCount(); i++ ) {
            message.additionalRecords_.add(DNSRecord.decodeRecord(input, message));
        }

        return message;
    }


    /**
     * This method reads the pieces of a domain name starting from the current position of the input stream
     *
     * @param inputStream - stream that gives us the information we need
     * @return - returns a string array that contains the domain name
     */
    public String[] readDomainName(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Initialize a length variable
        byte length = dataInputStream.readByte();

        // Initialize an array list of byte arrays to store all the characters of the url
        ArrayList<byte[]> domainParts = new ArrayList<>();

        // Loop through the q name to get the domain name
        while ( length != 0 ) {

            // Byte array to store each character of the domain name
            byte[] characters = new byte[length];

            // Fill the array
            for ( int i = 0; i < length; i++ ) {
                characters[i] = dataInputStream.readByte();
            }
            // Add the character to the array list
            domainParts.add(characters);

            // Read in the first byte which is the length of the domain name
            length = dataInputStream.readByte();
        }

        // Create a string array to store the parts of the domain name
        String[] parts = new String[domainParts.size()];

        // Loop through the domain parts and convert from decimal numbers to their ascii character
        for ( int i = 0; i < domainParts.size(); i++ ) {
            StringBuilder values = new StringBuilder();
            for ( byte b : domainParts.get(i) ) {
                values.append((char) b);
            }

            parts[i] = values.toString();
        }

        return parts;
    }


    /**
     * Same method as above but used when there's compression and we need to find the domain from earlier in the message.
     * This method should make a ByteArrayInputStream that starts at the specified byte and call the other version of this method
     *
     * @param firstByte - an integer value that will point to the correct part of the message to return the domain name
     * @return - returns an array of strings that contains each part of the domain name
     */
    public String[] readDomainName(int firstByte) throws IOException {
        // Create byte array input stream that takes the complete DNS message
        ByteArrayInputStream byteStream = new ByteArrayInputStream(completeMessage_, firstByte, completeMessage_.length - firstByte);

        // Return the string array with the domain name
        return readDomainName(byteStream);
    }


    /**
     * Method that builds a response based on the request and the answers needed to send back to the client
     *
     * @param request - a DNSMessage object
     * @param answers - an array list of DNSRecord objects that include the answers from the request
     * @return - returns a DNSMessage with all the necessary information for the response to the client or google
     */
    public static DNSMessage buildResponse(DNSMessage request, ArrayList<DNSRecord> answers) {
        DNSMessage response = new DNSMessage();
        response.header_ = DNSHeader.buildHeaderForResponse(request, response);
        response.questions_ = request.questions_;
        response.answers_ = answers;
        response.records_ = request.records_;
        response.additionalRecords_ = request.additionalRecords_;

        return response;
    }


    /**
     * Transform all the DNSMessage object information into a byte array that can be sent to the client or google
     *
     * @return - returns a byte array of all the DNSMessage object information
     */
    public byte[] toBytes() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HashMap<String, Integer> map = new HashMap<>();

        // Write out the header
        header_.writeBytes(output);

        // Write out the questions
        for ( DNSQuestion question : questions_ ) {
            question.writeBytes(output, map);
        }

        // Write out the answers
        for ( DNSRecord answer : answers_ ) {
            answer.writeBytes(output, map);
        }

        // Write out authority records
        for ( DNSRecord authority : records_ ) {
            authority.writeBytes(output, map);
        }

        // Write out the additional records
        for ( DNSRecord additional : additionalRecords_ ) {
            additional.writeBytes(output, map);
        }

        return output.toByteArray();
    }


    /**
     * This method writes the domain name to the output stream if it's the first time we've seen the specific domain packet.
     * Write it using the DNS encoding (each segment of the domain prefixed with its length, 0 at the end),
     * and add it to the hash map. Otherwise, write a back pointer to where the domain has been seen previously.
     *
     * @param outputStream    - stream for writing out the message
     * @param domainLocations - a hashmap that contains all of our questions and answers
     * @param domainPieces    - a string array that contains each piece of the domain name
     */
    public static void writeDomainName(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations, String[] domainPieces) throws IOException {
        // If this is the first time seeing the domain name, write it using the DNS encoding ( 10example.com0 )
        DNSMessage message = new DNSMessage();

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Join the pieces of the domain name
        String domainName = message.joinDomainName(domainPieces);

        // Create a key variable that will help us search for the domain name we want
        int key;

        // Check if we have the domain name requested
        if ( domainLocations.containsKey(domainName) ) {
            key = domainLocations.get(domainName);
            short compression = (short) (key | 0xC000);
            dataOutputStream.write(compression >> 8);
            dataOutputStream.write((byte) compression);
        } else {
            domainLocations.put(domainName, outputStream.toByteArray().length);
            for ( String s : domainPieces ) {
                dataOutputStream.writeByte(s.length());
                dataOutputStream.write(s.getBytes());
            }
            dataOutputStream.writeByte(0);

        }
    }


    /**
     * This method joins the pieces of a domain name with dots ([ "utah", "edu"] -> "utah.edu" )
     *
     * @param pieces - a string array of the pieces of the domain name
     * @return - returns a string with the domain name
     */
    String joinDomainName(String[] pieces) {
        return String.join(".", pieces);
    }


    /**
     * To string method converts the DNSMessage object into its string representation
     *
     * @return - returns a string with the contents of the DNSMessage object
     */
    @Override
    public String toString() {
        return "DNSMessage{" +
                "header=" + header_ +
                ", questions=" + questions_ +
                ", answers=" + answers_ +
                ", records=" + records_ +
                ", additionalRecords=" + additionalRecords_ +
                '}';
    }


    /**
     * The following six methods are all getter methods for the DNSMessage member variables
     */
    public DNSHeader getHeader() {
        return header_;
    }


    public ArrayList<DNSQuestion> getQuestions() {
        return questions_;
    }


    public ArrayList<DNSRecord> getAnswers() {
        return answers_;
    }


    public ArrayList<DNSRecord> getRecords() {
        return records_;
    }


    public ArrayList<DNSRecord> getAdditionalRecords() {
        return additionalRecords_;
    }


    public byte[] getCompleteMessage() {
        return completeMessage_;
    }
}
