import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Room class which stores the information for the rooms created by the client.
 * Each client will join a room and in that room they are
 * able to send messages to other users in the same room. The room class is also able to add
 * or remove clients from the room.
 */
public class Room {
    private String roomName_;
    static HashMap<String, Room> allRooms_ = new HashMap<>();
    static HashMap<String, Socket> allUsers_ = new HashMap<>();
    private ArrayList<String> messages_;


    /**
     * Constructor
     *
     * @param nameOfRoom - takes in a string which is the name of the room
     */
    private Room(String nameOfRoom) {
        this.roomName_ = nameOfRoom;
        this.messages_ = new ArrayList<>();
    }


    /**
     * AddAClient method adds a client to the room.
     *
     * @param userName - a string that contains the username
     * @param client   - a Socket object that allows us to communicate with the client
     * @param msg      - a string that contains the clients message
     */
    public synchronized void addAClient(String userName, Socket client, String msg) {
        allUsers_.put(userName, client);
    }


    /**
     * SendMessage method sends a message to all clients in the room
     *
     * @param message - a string that contains the message to be sent
     */
    public synchronized void sendMessage(String message) {
        // Loop through my map of all users
        for ( Map.Entry<String, Socket> entry : allUsers_.entrySet() ) {
            // Create a socket that stores the value of client socket
            Socket socket = entry.getValue();

            try {
                // Data output stream that will stream out the message in bytes
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                // Send the first byte of the header
                dataOut.writeByte(0x81);
                // Send the length of the message
                dataOut.writeByte(message.length()); // <- not correct for len > 125
                // Send the message
                dataOut.writeBytes(message);

            } catch ( Exception e ) {
                System.out.println("Error");
            }
        }
    }


    /**
     * RemoveClient method that removes a client from a room
     *
     * @param userName - a string that contains the username of the client to be removed
     * @param client   - socket object that helps us remove the client
     */
    public synchronized void removeClient(String userName, Socket client) {
        allUsers_.remove(userName, client);
    }


    /**
     * GetRoom method takes in a string which is the name of the room, checks to see if that room
     * already exists, if so, it returns the room that already exists, if not, it creates a new
     * room and returns that new room object
     *
     * @param name - string that is the name of the Room
     * @return - A room object that contains the name of requested room if it already exists or
     * a room object with the requested name
     */
    public synchronized static Room getRoom(String name) {
        // Variable that will store the name of the room if it doesn't already exist
        Room newRoom;
        // Room object that stores the name of the room given to us
        Room doesExist = new Room(name);
        // Check my map of all rooms and if the room already exists, return that room
        if ( allRooms_.containsValue(doesExist) ) {
            return doesExist;
        } else {
            // Create the room by assigning it to the nameOfRoom variable
            newRoom = new Room(name);
            // Add it to the list of rooms
            allRooms_.put(name, newRoom);
        }

        return newRoom;
    }

}
