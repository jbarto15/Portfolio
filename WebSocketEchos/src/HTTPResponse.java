import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * HTTPResponse class gives a response to the client based off their
 * request type. If the request is a file request then the response finds the requested file and then
 * sends it back to the client. If the request is a web socket request, the response class creates a
 * websocket connection, receives the incoming message, adds them to a room, and sends the message
 * to all clients in that room.
 */
class HTTPResponse {
    private String filename_;
    private File htmlFile_;
    private FileInputStream inputFile_;
    private FileInputStream inputErrorFile_;
    private OutputStream outputStream_;
    private String errorFilePath_;
    private HTTPRequest request_;
    private PrintWriter output_;
    private Socket clientSocket_;

    // Room variable to be used to determine where messages should be sent
    private Room room_;


    /**
     * Constructor for a HTTPResponse object
     *
     * @param filename     - requested filename
     * @param outputStream - outputstream to stream out information
     * @param request      - the HTTPRequest object that was generated from the request
     * @param client       - socket object to send the appropriate information to the client
     * @throws IOException
     */
    public HTTPResponse(String filename, OutputStream outputStream, HTTPRequest request, Socket client) throws IOException {

        this.outputStream_ = outputStream;
        this.filename_ = filename;
        this.htmlFile_ = new File("/Users/joshbarton/Desktop/MSD2023/CS6011/Week4/Day18/WebSocketEchos/resources/" + filename);
        this.inputFile_ = new FileInputStream(this.htmlFile_);
        this.errorFilePath_ = "resources/errorHtml.html";
        this.inputErrorFile_ = new FileInputStream(errorFilePath_);
        this.request_ = request;
        this.output_ = new PrintWriter(outputStream);
        this.clientSocket_ = client;

        // Create an exception if the htmlFile does not exist
        if ( !htmlFile_.exists() ) {
            IOException e = new IOException("File requested does not exist!");
            throw e;
        }
    }


    /**
     * Send Response method sends a response to the client
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws Exception
     */
    public void sendResponse() throws IOException, NoSuchAlgorithmException, Exception {
        // Boolean that checks if the web socket key is in my header info hash map
        boolean isWebSocket = request_.headerInfo_.containsKey("Sec-WebSocket-Key");

        // If the request is a websocket request, open the web socket chat
        if ( isWebSocket ) {
            openChat();
        }

        // If the request is a file request and not a web socket request, send back the files requested
        if ( htmlFile_.exists() && !isWebSocket ) {
            // Give the header information to the client
            outputStream_.write("HTTP/1.1 200 OK\n".getBytes());

            // Send out the type of file that will be sent
            String[] parts = filename_.split("\\.");
            String filetype = parts[parts.length - 1];
            outputStream_.write(("Content-Type: text/" + filetype + "\n").getBytes());
            outputStream_.write("\n".getBytes());

            for ( int i = 0; i < htmlFile_.length(); i++ ) {
                outputStream_.write(inputFile_.read());
                outputStream_.flush();
            }

            // Flush the output stream
            //outputStream.flush();

            // Close the output stream
            outputStream_.close();

        }
        // If we don't have the file they requested, send back the error header info and error file
        else {
            // Give the header information to the client and the file
            outputStream_.write("HTTP/1.1 404 Not Found\n".getBytes());
            outputStream_.write("Content-Type: text/html\n".getBytes());
            outputStream_.write("\n".getBytes());
            outputStream_.write("404 Not Found\n".getBytes());
            inputErrorFile_.transferTo(outputStream_);

            // Flush the output stream
            outputStream_.flush();

            // Close the output stream
            outputStream_.close();
        }
    }


    /**
     * OpenChat method opens a websocket and sends the web socket responses.
     *
     * @throws Exception
     */
    public void openChat() throws Exception {
        sendWebSocketHeader();

        // Start talking binary over the websocket with the client
        while ( true ) {
            String message = readInWebSocketData();
            sendChatMessage(message);
        }
    }


    /**
     * SendWebSocketHeader method sends the header information to the web socket client
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public void sendWebSocketHeader() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // Store the magic string that will be added to the web socket key
        String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        // Store the encode key
        String encodeKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
                .digest((request_.headerInfo_.get("Sec-WebSocket-Key") + magicString).getBytes("UTF-8")));
        // Send the appropriate header info
        output_.print("HTTP/1.1 101 Switching Protocols\r\n");
        output_.print("Upgrade: websocket\r\n");
        output_.print("Connection: Upgrade\r\n");
        output_.print("Sec-WebSocket-Accept: " + encodeKey + "\r\n");
        output_.print("\r\n"); // send blank line / end of headers
        output_.flush();
        // Check to see if the header was sent
        System.out.println("Header was sent");
    }


    /**
     * ReadInWebSocketData method reads in the data from the websocket and returns a string
     * with the message that was sent
     *
     * @return - a string with the message that was sent
     * @throws Exception
     */
    public String readInWebSocketData() throws Exception {
        // Data input stream that will read in the bytes from the client socket
        DataInputStream inData = new DataInputStream(clientSocket_.getInputStream());
        // Read in the first byte
        byte b0 = inData.readByte();
        // Read in the second byte
        byte b1 = inData.readByte();

        // Get the opcode and store in opcode variable
        int opcode = b0 & 0x0F;

        // Get the payload length by doing bitwise & operation on b1
        int length = b1 & 0x7F;

        // Check to see if the payload length is shorter than 126, if so, the length is equal to b1 & 0x7F
        if ( length < 126 ) {
            length = b1 & 0x7F;
        } else if ( length == 126 ) {
            length = inData.readShort();
        } else {
            length = (int) inData.readLong();
        }

        // Boolean variable that lets us know if we have a mask or not
        boolean hasMask = ((b1 & 0x80) != 0);

        // If there is not a mask, then print an error
        if ( !hasMask ) {
            System.out.println("Error!");
            throw new Exception("Unmasked message from the client.");
        }

        // Read in the next 4 bytes
        byte[] mask = inData.readNBytes(4);
        // Read in the payload using the length variable because that helps us know how many bytes to read
        byte[] payload = inData.readNBytes(length);

        // Unmask the message using the unmasking formula
        for ( int i = 0; i < payload.length; i++ ) {
            payload[i] = (byte) (payload[i] ^ mask[i % 4]);
        }

        // Turn the message into a string
        String message = new String(payload);
        System.out.println("Just got this message: " + message);

        return message;
    }


    /**
     * SendChatMessage sends the message back to the client.
     *
     * @param message - takes in a string as the message
     */
    public void sendChatMessage(String message) {
        // Get the room name, user, and type from the message
        String roomName = message.split("\"room\":\"")[1].split("\"")[0];
        String user = message.split("\"user\":\"")[1].split("\"")[0];
        String type = message.split("\"type\":\"")[1].split("\"")[0];

        // If the type is join, then add the client to the room and send a message to everyone in the
        // room that the new user has joined
        if ( type.equals("join") ) {
            room_ = Room.getRoom(roomName);
            this.room_.addAClient(user, clientSocket_, message);
            this.room_.sendMessage(message);
        }
        // If type is leave, remove user from the room and send a message to all clients in that room
        // that the user has left
        else if ( type.equals("leave") ) {
            this.room_.removeClient(user, clientSocket_);
            this.room_.sendMessage(message);
        }
        // Send the message to everyone in the room
        else {
            this.room_.sendMessage(message);
        }
    }
}
