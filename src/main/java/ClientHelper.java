import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * Impl of all the functions provided by the clients/peers
 */
public class ClientHelper {

    /***
     * Generic method to send message to any remote machine (server or client)
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
    public static void processMessage(SocketConnection sc, String message) throws IOException {
        String [] msgs = message.split(":");
        switch (msgs[0]) {
            case "Download":
                //Download a file and send to the requester
                break;
            case "GetLoad":
                //send the current load to requester
                break;
        }
    }


}
