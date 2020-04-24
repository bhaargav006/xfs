import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

/***
 * Impl of all the functions that will be provided by the Tracking Server
 */
public class ServerHelper {

    public ServerHelper() {

    }

    /**
     * This function returns the list of Nodes containing the file
     */
    public static Set<Integer> find(String fileName) {

        return null;
    }

    /**
     * This function updates the list of files and nodes
     */
    public static void updateList() {

    }

    /***
     * Generic method to send message to any client)
     * @param oos
     * @param message
     * @throws IOException
     */
    public static void sendMessage(ObjectOutputStream oos, String message) throws IOException {
        oos.writeUTF(message);
    }

    /***
     * Generic method to receive and parse message from both peer as well as server
     * @throws IOException
     */
    public static void processMessage(SocketConnection client, String message) throws IOException {
        String [] msgs = message.split(":");
        switch (msgs[0]) {
            case "UpdateList":
                //Request to update the map in the server
                break;
            case "Find":
                // Get the Nodes list for a particular file
                break;
        }
    }
}
