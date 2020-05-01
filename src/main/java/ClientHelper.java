
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static java.lang.Integer.MAX_VALUE;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
            System.out.println("Error: Couldn't send the " + message + " to output stream");
        }
    }

    /***
     * Generic method to receive and parse message from both peer as well as server
     */
    public static void processMessage(SocketConnection sc, String message, int myPort) {
        String [] msgs = message.split(" ");
        switch (msgs[0]) {
            case "Download":
                Client.currentLoad.getAndIncrement();
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
            sendMessage(sc.getOos(),fileContent);
        }
    }

    public static byte[] getFile(String fName, int port){
        File myFile = new File("C:\\Users\\Garima\\IdeaProjects\\xfs\\src\\main\\" + port + "\\" + fName);
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
        File folder = new File("C:\\Users\\Garima\\IdeaProjects\\xfs\\src\\main\\" + port);
        File[] fileNames = folder.listFiles();
//        System.out.println("FileNames: " + fileNames);
        if(fileNames != null){
            for(File f : fileNames) {
//                System.out.println(f.getName());
                byte[] content = readFileContent(f);
                String checksum = getChecksum(content);
                String fileName = f.getName() + ":-" + checksum;
                listOfFiles.add(fileName);
            }
        }
        return listOfFiles;
    }

    public static byte[] readFileContent(File f) {
        int len = (int)f.length();
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] content = new byte[len];
            fis.read(content);
            fis.close();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        if(listOfFiles != null || !listOfFiles.isEmpty()){
            ClientHelper.sendMessage(oos,listOfFiles);
        }
    }

    /***
     * Send the find request to the tracking server
     * @param tracker = socket to tracking server
     * @param fName = filename the peer is looking for
     * @return list of peers that has the file the caller is looking for
     */
    public static List<Integer> sendFindRequest(SocketConnection tracker, String fName) {
        System.out.println("Find File: " + fName);
        List<Integer> listOfPeers = new ArrayList<>();
        ClientHelper.sendMessage(tracker.getOos(),"Find " + fName);
        ObjectInputStream ois = tracker.getOis();
        try {
            String msg = (String) ois.readObject();
            System.out.println("Message: " + msg);
            if(msg.equalsIgnoreCase("null")) {
                System.out.println("File does not exist. ");
                return null;
            }
            System.out.println("Size of peerList: " + msg);
            Set<Integer> setOfPeers = (Set<Integer>) ois.readObject();
            for(int i : setOfPeers){
                listOfPeers.add(i);
            }
//            return (List<Integer>) ois.readObject();
            return listOfPeers;
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
                loadFromAllPeers.put(peerList.get(i), ((AtomicInteger)ois.readObject()).get());
                System.out.println("Load is here!");
                socketConnection.close();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: Could not connect to peer: " + peerList.get(i));
            }
        }

        return loadFromAllPeers;
    }

    /***
     * This function process the message from client after client inputs the file name to download.
     * @param trackingServerSocket = Server socket for finding the file using server
     * @param fName = Name of the file the user wants to download
     * @param port = port of the running peer
     */
    public static void DownloadFileFromPeers(SocketConnection trackingServerSocket, String fName, int port) {
        List<Integer> peerList = ClientHelper.sendFindRequest(trackingServerSocket, fName);
        if(peerList==null)
            return;

        /* For Testing purposes
        List<Integer> peerList = new ArrayList<>();
        peerList.add(8002);
        peerList.add(8003);
        */
        HashMap<Integer, Integer> loadFromAllPeers = getLoadFromPeers(peerList);
        int flag=0;

        while(!peerList.isEmpty() && flag==0){

            int idealPeer = ClientHelper.findIdealPeer(peerList, port, loadFromAllPeers);
            if(idealPeer!=-1){
                try {
                    SocketConnection idealSock = new SocketConnection(idealPeer);
                    sendMessage(idealSock.getOos(),"Download " + fName);
                    String answer = (String)idealSock.getOis().readObject();
                    System.out.println("Answer: " +answer);
                    if(answer.equalsIgnoreCase("null")){
                        peerList.remove(idealPeer);
                    }
                    else {
                        flag=1;
                        byte[] fileContent = (byte[])idealSock.getOis().readObject();
                        FileOutputStream fStream = new FileOutputStream("C:\\Users\\Garima\\IdeaProjects\\xfs\\src\\main\\" + port + "\\" + fName);
                        fStream.write(fileContent,0,Integer.parseInt(answer));
                    }
                    idealSock.close();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error: Couldn't download file from peer: " + idealPeer);
                }

            }
            else {
                System.out.println("Error: There are no ideal peers to download this file");
                return;
            }
        }
    }

    /***
     * Makes a decision to choose the ideal peer from peerList
     * The weight is inversely proportional to both latency and load.
     * @param peerList = list of all peers having the data
     * @param port = port of the running peer
     * @param loadFromAllPeers
     * @return returns the port of the ideal peer
     */
    public static int findIdealPeer(List<Integer> peerList, int port, HashMap<Integer, Integer> loadFromAllPeers) {
        //TODO : NULL checks to see if things are not breaking.
        int min=MAX_VALUE;
        File file = new File("C:\\Users\\Garima\\IdeaProjects\\xfs\\src\\main\\java\\latency.properties");
        FileInputStream fileInputStream = null;
        int idealPeer=-1;
        try {
            fileInputStream = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(fileInputStream);
            for(int peer:peerList){
                if(peer != port){
                    System.out.println("Peer: " +peer);
                    System.out.println("Port: " +port);
                    System.out.println(prop.getProperty(port+"."+peer));
                    int weight = loadFromAllPeers.get(peer)*Integer.parseInt(prop.getProperty(port+"."+peer));
                    if(weight < min){
                        min = weight;
                        idealPeer=peer;
                    }
                }
            }
            return idealPeer;
        } catch (FileNotFoundException e) {
            System.out.println("Error: Couldn't locate the properties file");
        } catch (IOException e) {
            System.out.println("Error: Couldn't read the properties file");
        }

        return -1;
    }

    /***
     * function to generate checksum for the contents of the file
     * @param fileContentBytes : all the content
     * @return checksum string
     */
    public static String getChecksum(byte[] fileContentBytes) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(fileContentBytes);
            byte[] digest = md5.digest();
            String checksum = DatatypeConverter.printHexBinary(digest);
            return checksum;
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
