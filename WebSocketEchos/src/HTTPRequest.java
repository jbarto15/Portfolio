import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * HTTPRequest class is responsible for taking in client requests and storing the data from the request
 * It contains methods that return header information and the name of the filename that was requested
 */
public class HTTPRequest {
    private InputStream requestFromClient_;
    private String filename_;
    public HashMap<String, String> headerInfo_;


    /**
     * Constructor
     *
     * @param clientRequest - an inputstream object that contains HTTP header information
     */
    public HTTPRequest(InputStream clientRequest) {
        requestFromClient_ = clientRequest;
        headerInfo_ = new HashMap<>();
    }


    /**
     * This method gets the file name by scanning the client request, taking the resulting string,
     * splitting it up, and adding the info to the hash map member variable.
     */
    public void getHeaderInfo() {
        Scanner scanner = new Scanner(requestFromClient_);
        filename_ = "";

        if ( scanner.hasNext() ) {
            // Store the client request in client request variable
            String clientRequest = scanner.nextLine();

            // Parse the client request into parts
            String[] parts = clientRequest.split(" ");
            filename_ = parts[1];
            // If only a / is given from the client, assign that slash to /webclient.html
            if ( parts[0].equals("GET") && parts[1].equals("/") ) {
                parts[1] = "/webclient.html";
                filename_ = parts[1];
            }
        }

        while ( true ) {
            // Create a string that will store the next line of the request
            String line = scanner.nextLine();

            // If the line is empty, break the while loop because we don't need additional header info
            if ( line.isEmpty() ) {
                break;
            }

            // Parse the client request into parts based on where there is a space and store it as an array of strings
            String[] parts = line.split(": ");

            // Store the first part of the request as the keyword for the map, and the second part as the value
            headerInfo_.put(parts[0], parts[1]);
        }

        // If the filename is empty, put a / before it, so I don't go into my resources folder
        if ( filename_.isEmpty() ) {
            filename_ = "/";
        }

    }


    /**
     * This is a getter method that returns my filename member variable
     *
     * @return - returns the name of the file that has been requested
     */
    public String getFileName() {
        return filename_;
    }

}




