import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;

/***
 * Impl of all the functions that will be provided by the Tracking Server
 */
public class ServerHelper {

    public ServerHelper() {

    }

    /***
     * Generic method to receive and parse message from both peer as well as server
     * @throws IOException
     */
    public static void processMessage(SocketConnection client, String message){
        String [] msgs = message.split(":");
        switch (msgs[0]) {
            case "UpdateList":
                receiveFileList(client.getOis());
                break;
            case "Find":
                sendListOfPeers(client, msgs[1]);
                break;
        }
    }

    public static void receiveFileList(ObjectInputStream ois){

        try {
            String peer= (String) ois.readObject();
            ArrayList<String> listOfFiles= (ArrayList<String>) ois.readObject();
            updateList(peer,listOfFiles);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: Couldn't receive fileList from the peer");;
        }
    }
    /**
     * This function updates the list of files and nodes
     */
    public static void updateList(String peer, ArrayList<String> listOfFiles) {

    }

    /***
     * Sends list of peers to the calling peer looking for a file
     * @param client = calling peer
     * @param filename = filename
     */
    private static void sendListOfPeers(SocketConnection client, String filename) {
        Set<Integer> listOfPeers = TrackingServer.listOfFiles.get(filename);
        if(listOfPeers==null)
            sendMessage(client.getOos(),"null");
        else {
            sendMessage(client.getOos(),String.valueOf(listOfPeers.size()));
            sendMessage(client.getOos(),listOfPeers);
        }
    }

    /***
     * Generic method to send message to any client)
     * @param oos = client's output stream
     * @param message = Message object that has to sent through TCP
     * @throws IOException
     */
    public static void sendMessage(ObjectOutputStream oos, Object message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            System.out.println("Couldn't send the message to client");
        }
    }


}
