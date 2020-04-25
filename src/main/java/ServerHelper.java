import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/***
 * Impl of all the functions that will be provided by the Tracking Server
 */
public class ServerHelper {

    public ServerHelper() {

    }

    /***
     * Generic method to receive and parse message from both peer as well as server
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
            Integer peer= (Integer) ois.readObject();
            ArrayList<String> listOfFiles= (ArrayList<String>) ois.readObject();
            updateList(peer,listOfFiles);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: Couldn't receive fileList from the peer");;
        }
    }

    /***
     * This is used to update the listofFileOwners global variable
     * @param peer = owner of listOfFiles
     * @param listOfFiles = listofFiles received by the peer
     */
    public static void updateList(Integer peer, ArrayList<String> listOfFiles) {
        for(String file : listOfFiles){
            if(TrackingServer.listOfFileOwners.get(file)==null){
                Set<Integer> owner = new HashSet<>();
                owner.add(peer);
               TrackingServer.listOfFileOwners.put(file,owner);
            }
            else TrackingServer.listOfFileOwners.get(file).add(peer);
        }
    }

    /***
     * Sends list of peers to the calling peer looking for a file
     * @param client = calling peer
     * @param filename = filename
     */
    private static void sendListOfPeers(SocketConnection client, String filename) {
        Set<Integer> listOfPeers = TrackingServer.listOfFileOwners.get(filename);
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
