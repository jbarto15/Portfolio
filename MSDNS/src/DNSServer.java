import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DNSServer {
    private int server_port_ = 8053;
    private int google_port_ = 53;
    private DatagramSocket socket_;
    private byte[] buffer_;
    private DatagramPacket clientPacket_;
    private DNSCache cache_;
    private InetAddress googleAddress_;


    public DNSServer() throws SocketException {
    }


    /**
     * Start method starts the DNSServer
     *
     * @throws IOException
     */
    public void start() throws IOException {

        server_port_ = 8053;
        System.out.println("Listening at " + server_port_);
        socket_ = new DatagramSocket(server_port_);
        buffer_ = new byte[512];
        clientPacket_ = new DatagramPacket(buffer_, buffer_.length);

        cache_ = new DNSCache();

        while ( true ) {

            // Wait to receive the packet from the client
            socket_.receive(clientPacket_);

            // Decode the client's packet
            DNSMessage message = DNSMessage.decodeMessage(clientPacket_.getData());

            // Loop through all the questions in the client packet and see if we have it in the cache
            for ( int i = 0; i < message.getQuestions().size(); i++ ) {
                if ( cache_.query(message.getQuestions().get(i)) != null ) {
                    // Send the response to the client
                    DNSMessage response = DNSMessage.buildResponse(message, message.getAnswers());
                    // Alter the response from a DNSMessage to an array of bytes
                    byte[] responseByteArray = response.toBytes();
                    // Send the response
                    DatagramPacket responsePacket = new DatagramPacket(responseByteArray, responseByteArray.length);
                    socket_.send(responsePacket);
                } else {
                    // Send the packet to google
                    googleAddress_ = InetAddress.getByName("8.8.8.8");
                    DatagramPacket requestToGoogle = new DatagramPacket(buffer_, clientPacket_.getLength(), googleAddress_, google_port_);
                    socket_.send(requestToGoogle);

                    // Wait for Google response, create a new buffer and packet for the Google response packet
                    byte[] googleBuffer = new byte[512];
                    DatagramPacket packetFromGoogle = new DatagramPacket(googleBuffer, googleBuffer.length);
                    socket_.receive(packetFromGoogle);

                    // Decode google's response
                    DNSMessage googleMessage = DNSMessage.decodeMessage(packetFromGoogle.getData());

                    // Call the isDomainNameValid function here to make sure the domain name requested is valid
                    boolean isDomainValid = DNSServer.isDomainNameValid(googleMessage.getHeader().getFlag_());

                    if ( isDomainValid && !googleMessage.getQuestions().isEmpty() && !googleMessage.getAnswers().isEmpty() ) {

                        // Add response to the cache     **this portion could cause problems I think, might need some sort of loop
                        cache_.insert(googleMessage.getQuestions().get(i), googleMessage.getAnswers().get(i));

                        // Send googles response to the client
                        byte[] googleResponseArray = packetFromGoogle.getData();
                        DatagramPacket packetToClient = new DatagramPacket(googleResponseArray, packetFromGoogle.getLength(), clientPacket_.getAddress(), clientPacket_.getPort());
                        socket_.send(packetToClient);
                    } else {
                        // Send error message to client saying the domain does not exist
                        byte[] errorResponseArray = packetFromGoogle.getData();
                        DatagramPacket errorPacketToClient = new DatagramPacket(errorResponseArray, packetFromGoogle.getLength(), clientPacket_.getAddress(), clientPacket_.getPort());
                        socket_.send(errorPacketToClient);
                    }
                }
            }
        }
    }


    /**
     * IsDomainNameValid method that checks for a valid URL
     *
     * @param flag - takes a flag that helps us determine if the domain name is valid or not
     * @return - returns true or false depending on if the domain name is valid or not
     */
    static boolean isDomainNameValid(short flag) {
        // Check if the last two bits of the flag are equal to 1,1. If so, the domain name is not valid
        short lastTwoBits = (short) (flag & 0x000F);

        return lastTwoBits != 0x0004;
    }
}
