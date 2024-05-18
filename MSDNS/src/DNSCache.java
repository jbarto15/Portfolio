import java.util.HashMap;

public class DNSCache {
    HashMap<DNSQuestion, DNSRecord> cache_ = new HashMap<>();


    /**
     * Query method that queries records in the cache
     *
     * @param question - a DNSQuestion object
     * @return - returns a DNSRecord object
     */
    public DNSRecord query(DNSQuestion question) {

        DNSRecord record = cache_.get(question);

        if ( record != null && !record.isExpired() ) {
            return record;
        }

        if ( record != null && record.isExpired() ) {
            cache_.remove(question);
            return null;
        }

        return null;
    }


    /**
     * Insert method which inserts a record into the cache
     *
     * @param question - a DNSQuestion object
     * @param record   - a DNSRecord object
     */
    public void insert(DNSQuestion question, DNSRecord record) {

        if ( !cache_.containsKey(question) ) {
            cache_.put(question, record);
        }

    }


    /**
     * To string method which turns the DNSCache object into its string representation
     *
     * @return - returns a string with the contents of the DNSCache object
     */
    @Override
    public String toString() {
        return "DNSCache{" +
                "cache=" + cache_ +
                '}';
    }
}
