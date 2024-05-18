import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DNSRecord {
    private String[] name_;
    private short type_;
    private short class_;
    private int ttl_;
    private short rdlength_;
    private byte[] rdata_;
    private Date date_;
    private long timestamp_;


    /**
     * This method decodes the DNS request and gathers the record information
     *
     * @param inputStream - allows us to get the appropriate info
     * @param message     - takes in a DNSMessage object
     * @return - returns a DNSRecord
     */
    public static DNSRecord decodeRecord(InputStream inputStream, DNSMessage message) throws IOException {
        // Wrap the input stream in a data input stream
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Create a DNSRecord object
        DNSRecord record = new DNSRecord();

        // Mark start of offset
        dataInputStream.mark(2);

        // Read in name and check for compression bits
        short firstBytes = dataInputStream.readShort();
        int compression = (short) firstBytes & 0xC000;

        // If the first two bits are 1:1, then we need to read the compressed domain name in
        if ( compression == 0xC000 ) {
            // Save the offset in a variable
            int offset = firstBytes & 0x3FFF;

            // Assign the record name with the domain name
            record.name_ = message.readDomainName(offset);
        } else {
            dataInputStream.reset();

            // Assign the record name with the domain name
            record.name_ = message.readDomainName(inputStream);
        }

        // Read in the rest of the record information
        record.type_ = dataInputStream.readShort();
        record.class_ = dataInputStream.readShort();
        record.ttl_ = dataInputStream.readInt();
        record.rdlength_ = dataInputStream.readShort();

        // Read in the r data based on the length of the r data
        record.rdata_ = dataInputStream.readNBytes(record.rdlength_);

        // Record the time the record was created
        record.date_ = new Date();
        record.timestamp_ = record.date_.getTime();

        return record;
    }


    /**
     * This method writes the DNS record information to the stream
     *
     * @param outputStream - allows us to write to the stream
     * @param map          - map allows us to access the domain name
     */
    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> map) throws IOException {
        DNSMessage.writeDomainName(outputStream, map, name_);
        outputStream.write(type_ >> 8);
        outputStream.write(type_);

        outputStream.write(class_ >> 8);
        outputStream.write(class_);

        outputStream.write(ttl_ >> 24);
        outputStream.write(ttl_ >> 16);
        outputStream.write(ttl_ >> 8);
        outputStream.write(ttl_);

        outputStream.write(rdlength_ >> 8);
        outputStream.write(rdlength_);

        outputStream.write(rdata_);
    }


    /**
     * This method helps us determine whether our stored records are expired or not.
     *
     * @return boolean - Return whether the creation date + the time to live is after the current time.
     */
    public boolean isExpired() {
        // Get the expired time which is the time the record was created + the time to live
        long expiredTime = timestamp_ + ttl_;

        // Get the current time
        Date date = new Date();
        long currentTime = date.getTime();

        return currentTime > expiredTime;
    }


    @Override
    public String toString() {
        return "DNSRecord{" +
                "name_='" + Arrays.toString(name_) + '\'' +
                ", type_=" + type_ +
                ", class_=" + class_ +
                ", ttl_=" + ttl_ +
                ", rdlength_=" + rdlength_ +
                ", rdata_=" + Arrays.toString(rdata_) +
                ", date_=" + date_ +
                ", timestamp_=" + timestamp_ +
                '}';
    }
}
