import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public static void sendMessage(ObjectOutputStream oos, Object message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static Set<Integer> findFile(){
        return null;
    }

    /***
     * Gets a list of files present in the directory of the client
     *
     * /8001 is the directory of client 8001
     * @param port
     * @return
     */
    public static List<String> getListOfFiles(int port){
        List<String> listOfFiles = new ArrayList<>();
        File folder = new File("/" + port);
        File[] fileNames = folder.listFiles();
        for(File f : fileNames)
            listOfFiles.add(f.getName());
        return listOfFiles;
    }

    /***
     * Sends the filesystem contents to the tracking server
     * @param oos = output stream to the tracking server
     * @param port = port to identify the sending peer
     * @param listOfFiles = list of files in the system
     */
    public static void sendFileSystemContent(ObjectOutputStream oos, int port, List<String> listOfFiles){
        ClientHelper.sendMessage(oos,"UpdateList");
        ClientHelper.sendMessage(oos,String.valueOf(8001));
        ClientHelper.sendMessage(oos,listOfFiles);
    }
}
