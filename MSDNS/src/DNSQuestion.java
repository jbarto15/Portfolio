import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    private String[] qName_;
    private short qType_;
    private short qClass_;


    /**
     * This method decodes a question from the input stream.
     *
     * @param inputStream - allows us to read in the appropriate information.
     * @return - DNSQuestion object
     */
    public static DNSQuestion decodeQuestion(InputStream inputStream, DNSMessage message) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        DNSQuestion question = new DNSQuestion();

        // Add the domain name to the qname variable
        question.qName_ = message.readDomainName(inputStream);

        // Read in the question type
        question.qType_ = dataInputStream.readShort();

        // Read in the question class
        question.qClass_ = dataInputStream.readShort();

        return question;
    }


    /**
     * This method writes the question bytes which will be sent to the client.
     * The hash map is used for us to compress the message.
     *
     * @param outputStream        - allows us to write to the stream
     * @param domainNameLocations - a hashmap with our questions and records from the request.
     */
    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainNameLocations) throws IOException {
        DNSMessage.writeDomainName(outputStream, domainNameLocations, qName_);
        outputStream.write(qType_ >> 8);
        outputStream.write(qType_);
        outputStream.write(qClass_ >> 8);
        outputStream.write(qClass_);
    }


    /**
     * To string method which converts the DNSQuestion object into its string representation
     *
     * @return - returns a string with the contents of the DNSQuestion object
     */
    @Override
    public String toString() {
        return "DNSQuestion{" +
                "qName_='" + Arrays.toString(qName_) + '\'' +
                ", qType_=" + qType_ +
                ", qClass_=" + qClass_ +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        DNSQuestion question = (DNSQuestion) o;
        return qType_ == question.qType_ && qClass_ == question.qClass_ && Arrays.equals(qName_, question.qName_);
    }


    @Override
    public int hashCode() {
        int result = Objects.hash(qType_, qClass_);
        result = 31 * result + Arrays.hashCode(qName_);
        return result;
    }
}
