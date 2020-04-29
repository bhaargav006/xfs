import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/***
 * Impl of all the functions provided by the clients/peers
 */
public class ClientHelper {

    /***
     * Generic method to send message to any remote machine (server or client)
     * @param oos = output stream to the target
     * @param message = message object that has to be sent
     */
    public static void sendMessage(ObjectOutputStream oos, Object message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            System.out.println("Error: Couldn't send the " + message + " to output stream");;
        }
    }

    /***
     * Generic method to receive and parse message from both peer as well as server
     */
    public static void processMessage(SocketConnection sc, String message, int myPort) {
        String [] msgs = message.split(":");
        switch (msgs[0]) {
            case "Download":
                Client.currentLoad++;
                DownloadFile(sc, getFile(msgs[1], myPort));
                break;

            case "GetLoad":
                sendMessage(sc.getOos(),Client.currentLoad);
                break;

            case "SendAll":
                getListOfFiles(myPort);
                sendFileSystemContent(sc.getOos(), myPort, getListOfFiles(myPort));
            break;
        }
    }

    private static void DownloadFile(SocketConnection sc, byte[] file) {
        byte[] fileContent = file;
        if(fileContent==null)
            sendMessage(sc.getOos(),"null");
        else {
            sendMessage(sc.getOos(),String.valueOf(fileContent.length));
            sendMessage(sc.getOos(),String.valueOf(fileContent));
        }
    }

    public static byte[] getFile(String fName, int port){
        File myFile = new File("/" + port + "/" + fName);
        try {
            FileInputStream fStream = new FileInputStream((myFile));
            byte[] fileContent = new byte[(int) myFile.length()];
            fStream.read(fileContent,0,(int)myFile.length());
            return fileContent;
        } catch (FileNotFoundException e) {
            System.out.println("Error: File is not present in this peer");
        } catch (IOException e) {
            System.out.println("Error: Can't read the file");
        }

        return null;
    }

    /***
     * Gets a list of files present in the directory of the client
     *
     * /8001 is the directory of client 8001
     * @param port = is the name of the directory belonging to the current peer
     * @return list of files
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
        ClientHelper.sendMessage(oos,port);
        ClientHelper.sendMessage(oos,listOfFiles);
    }

    /***
     * Send the find request to the tracking server
     * @param tracker = socket to tracking server
     * @param fName = filename the peer is looking for
     * @return list of peers that has the file the caller is looking for
     */
    public static List<Integer> sendFindRequest(SocketConnection tracker, String fName) {
        ClientHelper.sendMessage(tracker.getOos(),"Find " + fName);
        ObjectInputStream ois = tracker.getOis();
        try {
            String msg = (String) ois.readObject();
            if(msg.equalsIgnoreCase("null")) {
                System.out.println("File does not exist. ");
                return null;
            }
            System.out.println("Size of peerList: " + msg);
            List<Integer> peerList = (ArrayList<Integer>) ois.readObject();
            return peerList;
        } catch (IOException| ClassNotFoundException e) {
            System.out.println("Error: Couldn't get peerList form the tracking server");
        }
        return null;
    }


    /***
     * This functions gets load from all clients by creating a socket connection for each client.
     * @param peerList = list of clients that have the file
     * @return
     */
    public static HashMap<Integer, Integer> getLoadFromPeers(List<Integer> peerList) {
        HashMap<Integer, Integer> loadFromAllPeers = new HashMap<>();
        for(int i = 0; i < peerList.size(); i++){
            try {
                SocketConnection socketConnection = new SocketConnection(peerList.get(i));
                ObjectOutputStream oos = socketConnection.getOos();
                ObjectInputStream ois = socketConnection.getOis();

                ClientHelper.sendMessage(oos, "GetLoad");
                loadFromAllPeers.put(peerList.get(i), (Integer)ois.readObject());
                System.out.println("Load is here!");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: Could not connect to peer: " + peerList.get(i));
            }
        }

        return loadFromAllPeers;
    }

    /***
     * This function process the message from client after client inputs the file name to download.
     * @param trackingServerSocket = Server socket for finding the file using server
     * @param fname = Name of the file the user wants to download
     */
    public static void processMessageFromClient(SocketConnection trackingServerSocket, String fname) {
        List<Integer> peerList = ClientHelper.sendFindRequest(trackingServerSocket, fname);
        /* For Testing purposes
        List<Integer> peerList = new ArrayList<>();
        peerList.add(8002);
        peerList.add(8003);
        */
        HashMap<Integer, Integer> loadFromAllPeers = getLoadFromPeers(peerList);
        System.out.println("Function ran perfectly!");
    }

    /***
     * Makes a decision to choose the ideal peer from peerList
     * @param peerList = list of all peers having the data
     * @return returns the port of the ideal peer
     */
    public static int findIdealPeer(ArrayList<Integer> peerList) {
        return 0;
    }
}
