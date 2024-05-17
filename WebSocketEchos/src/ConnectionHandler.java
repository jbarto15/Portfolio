import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Connection handler class which passes the socket of each client to this connection
 * handler. The class creates an input and output stream object as well as an HTTPRequest and
 * HTTPResponse object. The request object is able to get header information and filenames.
 * The response object is able to send responses back to the client depending on what they are asking
 * for
 */
public class ConnectionHandler implements Runnable {
    private Socket client_;


    /**
     * Constructor
     *
     * @param client - a socket object
     */
    ConnectionHandler(Socket client) {
        client_ = client;
    }


    /**
     * Run method which runs each thread we create
     */
    @Override
    public void run() {
        try {
            InputStream inputStream = client_.getInputStream();
            OutputStream outputStream = client_.getOutputStream();

            HTTPRequest request = new HTTPRequest(inputStream);

            // Get the header information
            request.getHeaderInfo();

            // Store the filename from the request
            String filename = request.getFileName();
            System.out.println("found filename of: " + filename);


            HTTPResponse response = new HTTPResponse(filename, outputStream, request, client_);

            // Send the appropriate response to the client
            response.sendResponse();

        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }
}




