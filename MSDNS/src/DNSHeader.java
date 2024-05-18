import java.io.*;

public class DNSHeader {
    private short transactionID_;
    private short flag_;
    private short questionCount_;
    private short answerCount_;
    private short authorityRecordCount_;
    private short additionalRecordCount_;


    /**
     * This method decodes the header information from the DNS request. It uses a ByteArrayInputStream.
     *
     * @param inputStream - allows us to read the DNS request information in
     * @return - returns a DNSHeader object
     */
    public static DNSHeader decodeHeader(InputStream inputStream) throws IOException {
        // Create a header object to return
        DNSHeader header = new DNSHeader();

        // Convert input stream to data input stream to read in shorts/bytes/etc
        DataInputStream dataStream = new DataInputStream(inputStream);

        // Read in a short and store the header ID
        header.transactionID_ = dataStream.readShort();

        // Store the flag information
        header.flag_ = dataStream.readShort();

        header.questionCount_ = dataStream.readShort();
        header.answerCount_ = dataStream.readShort();
        header.authorityRecordCount_ = dataStream.readShort();
        header.additionalRecordCount_ = dataStream.readShort();

        return header;
    }


    /**
     * This method builds the header for when the server will respond to the DNS request. It will copy some fields from
     * the request.
     *
     * @param request  - a DNSMessage object that contains all the request info
     * @param response - a DNSMessage that will contain our response info
     * @return - returns a DNSHeader object
     */
    public static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) {

        // Change the QR bit from a 0 to a 1 since we're sending a response
        response.getHeader().flag_ = (short) (request.getHeader().flag_ | 0x8000);

        // Assign question count
        response.getHeader().questionCount_ = request.getHeader().questionCount_;

        // Assign the answer count to 1 since we can only send one response at a time
        response.getHeader().answerCount_ = 0x0001;

        // Assign authority record count
        response.getHeader().authorityRecordCount_ = request.getHeader().authorityRecordCount_;

        // Assign additional record
        response.getHeader().additionalRecordCount_ = request.getHeader().additionalRecordCount_;


        return response.getHeader();
    }


    /**
     * This method encodes the header to bytes to be sent back to the client. The OutputStream interface has methods to write a single byte or an array of bytes.
     *
     * @param outputStream - allows us to write bytes to the stream
     */
    public void writeBytes(OutputStream outputStream) throws IOException {
        outputStream.write(transactionID_ >> 8);
        outputStream.write(transactionID_);
        outputStream.write(flag_ >> 8);
        outputStream.write(flag_);
        outputStream.write(questionCount_ >> 8);
        outputStream.write(questionCount_);
        outputStream.write(answerCount_ >> 8);
        outputStream.write(answerCount_);
        outputStream.write(authorityRecordCount_ >> 8);
        outputStream.write(authorityRecordCount_);
        outputStream.write(additionalRecordCount_ >> 8);
        outputStream.write(additionalRecordCount_);

    }


    /**
     * This method returns a human-readable string version of a header object
     *
     * @return - returns a string of the entire object
     */
    @Override
    public String toString() {
        return "DNSHeader{" +
                "transactionID_=" + transactionID_ +
                ", flag_=" + flag_ +
                ", qdcount_=" + questionCount_ +
                ", ancount_=" + answerCount_ +
                ", nscount_=" + authorityRecordCount_ +
                ", arcount_=" + additionalRecordCount_ +
                '}';
    }


    /**
     * The following six methods are getter methods for the member variables in the DNSHeader
     */
    public short getTransactionID_() {
        return transactionID_;
    }


    public short getFlag_() {
        return flag_;
    }


    public short getQuestionCount_() {
        return questionCount_;
    }


    public short getAnswerCount_() {
        return answerCount_;
    }


    public short getAuthorityRecordCount() {
        return authorityRecordCount_;
    }


    public short getAdditionalRecordCount() {
        return additionalRecordCount_;
    }

}
