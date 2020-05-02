import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        String [] msgs = message.split(" ");
        System.out.println(message);
        switch (msgs[0]) {
            case "UpdateList":
                receiveFileList(client.getOis());
                break;
            case "Find":
                System.out.println("In Find case");
                sendListOfPeers(client, msgs[1]);
                break;
        }
    }

    public static void receiveFileList(ObjectInputStream ois){

        try {
            Integer peer= (Integer) ois.readObject();
            ArrayList<String> listOfFiles= (ArrayList<String>) ois.readObject();
            ArrayList<String> listOfFilesWOChecksum = new ArrayList<>();
            HashMap<String, String> filesAndCheckSum = new HashMap<>();
            for(String file: listOfFiles) {
                String[] fileCheckSum = file.split(":-");
                listOfFilesWOChecksum.add(fileCheckSum[0]);
                filesAndCheckSum.put(fileCheckSum[0],fileCheckSum[1]);
            }
            updateList(peer,listOfFilesWOChecksum);
            TrackingServer.filesToCheckSum.putAll(filesAndCheckSum);
            System.out.println("Done receiving files!");
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
        System.out.println("In send list of peers");
        Set<Integer> listOfPeers = TrackingServer.listOfFileOwners.get(filename);
        System.out.println(listOfPeers);
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

    public static List<Integer> peerList()  {
        File file = new File("C:\\Users\\Garima\\IdeaProjects\\xfs\\src\\main\\java\\peerList.properties");
        FileInputStream fis = null;
        Properties properties = null;
        try {
            fis = new FileInputStream(file);
            properties = new Properties();
            properties.load(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Enumeration keys = properties.keys();
        List<Integer> peerList = new ArrayList<>();
        while(keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Integer port = Integer.parseInt(properties.getProperty(key));
            peerList.add(port);
        }
        return peerList;
    }

    /***
     * Used to create the listof owners on startup of server or after if the server is recovered
     */
    public static void createListOfFileOwnersAndCheckSum()  {
        String msg = "SendAll";
        List<Integer> peerList = peerList();
        for(int peer: peerList) {
            SocketConnection sc = null;
            try {
                sc = new SocketConnection(peer);
                sendMessage(sc.getOos(), msg);
                if (sc.getOis().readObject().equals("UpdateList")) {
                    receiveFileList(sc.getOis());
                }
                sc.close();
            } catch (IOException e) {
                System.out.println("Peer" + peer + "is Not Available!");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Successfully Created the listOfFileOwners...");
    }
}
