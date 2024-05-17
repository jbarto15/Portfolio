import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is a webserver program that creates a server, waits for the client to connect and then
 * takes the client requests and either sends them a file that they requested, or it creates
 * a websocket that allows the client and server to exchange messages with one another
 */
public class webServer {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8080);

            // While loop that allows the server socket to always be looking for a client request
            while (true) {
                // Create socket variable that creates a socket on the server to be able to recognize the client
                Socket client = server.accept();
                // Create a thread that wraps the ConnectionHandler and takes the client socket as a parameter
                Thread thread = new Thread(new ConnectionHandler(client));
                // Start/run the thread
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}