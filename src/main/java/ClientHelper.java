
import javafx.util.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static java.lang.Integer.MAX_VALUE;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
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
        File myFile = new File("/Users/bhaargavsriraman/Desktop/UMN/Git/xfs/src/main/java/" + port + "/" + fName);
        try {
            FileInputStream fStream = new FileInputStream((myFile));
            byte[] fileContent = new byte[(int) myFile.length()];
            fStream.read(fileContent,0,(int)myFile.length());
            fStream.close();
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
//        Path relPath = Paths.get("8001");
//        System.out.println(relPath);
        File folder = new File("/Users/bhaargavsriraman/Desktop/UMN/Git/xfs/src/main/java/" + port);
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
     * @param fName = filename the peer is looking for
     * @return list of peers that has the file the caller is looking for
     */
    public static Pair<List<Integer>,String> sendFindRequest(String fName) {
        SocketConnection tracker = null;
        try {
            tracker = new SocketConnection(8000);
            System.out.println("Find File: " + fName);
            ClientHelper.sendMessage(tracker.getOos(),"Find " + fName);
            ObjectInputStream ois = tracker.getOis();

            String msg = (String) ois.readObject();
            System.out.println("Message: " + msg);
            if(msg.equalsIgnoreCase("null")) {
                System.out.println("File does not exist. ");
                return null;
            }
            System.out.println("Size of peerList: " + msg);
            Set<Integer> setOfPeers = (Set<Integer>) ois.readObject();
            String checksum = (String) ois.readObject();
            tracker.close();

            List<Integer> listOfPeers = new ArrayList<>(setOfPeers);
            return new Pair(listOfPeers,checksum);
        } catch (IOException| ClassNotFoundException e) {
            System.out.println("Server Down.... Couldn't get peerList...Waiting for the Server to come back");
            //blocking the client thread for server to come back
            Client.serverStatus = false;
            new ServerHealth().start();
            while(!Client.serverStatus) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            }
            System.out.println("Server is back online");
            System.out.println("Retrying FIND request....");
            sendFindRequest(fName);
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
     * @param fName = Name of the file the user wants to download
     * @param port = port of the running peer
     */
    public static void DownloadFileFromPeers(String fName, int port) {
        Pair<List<Integer>,String> findResult =  ClientHelper.sendFindRequest(fName);
        if(findResult==null)
            return;
        List<Integer> peerList = findResult.getKey();
        String checksum = findResult.getValue();

        HashMap<Integer, Integer> loadFromAllPeers = getLoadFromPeers(peerList);
        int flag=0;

        while(!peerList.isEmpty() && flag==0){

            Pair idealPair= ClientHelper.findIdealPeer(peerList, port, loadFromAllPeers);
            int idealPeer = (int) idealPair.getKey();
            if(idealPeer!=-1){
                try {
                    SocketConnection idealSock = new SocketConnection(idealPeer);
                    System.out.println("Contacting ideal peer. This will take some time...");
                    Thread.sleep((int) idealPair.getValue());
                    sendMessage(idealSock.getOos(),"Download " + fName);
                    String answer = (String)idealSock.getOis().readObject();
                    System.out.println("Answer: " +answer);
                    if(answer.equalsIgnoreCase("null")){
                        peerList.remove(idealPeer);
                    }
                    else {

                        byte[] fileContent = (byte[])idealSock.getOis().readObject();
                        Path currentDir = Paths.get(".");
                        System.out.println(currentDir);
                        FileOutputStream fStream = new FileOutputStream("/Users/bhaargavsriraman/Desktop/UMN/Git/xfs/src/main/java/" + port + "/" + fName);
                        fStream.write(fileContent,0,Integer.parseInt(answer));
                        fStream.close();
                        flag = checkChecksum(checksum,fileContent);
                        if(flag==0){
                            Files.deleteIfExists(Paths.get("/Users/bhaargavsriraman/Desktop/UMN/Git/xfs/src/main/java/" + port + "/" + fName));
                        }
                    }
                    idealSock.close();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error: Couldn't download file from peer: " + idealPeer);
                } catch (InterruptedException e) {
                    System.out.println("Error: thread couldn't be interrupted");
                }

            }
            else {
                System.out.println("Error: There are no ideal peers to download this file");
                return;
            }
        }
    }

    private static int checkChecksum(String checksum, byte[] fileContent) {
        String newChecksum = getChecksum(fileContent);
        if(checksum.equals(newChecksum))
            return 1;
        return 0;
    }

    /***
     * Makes a decision to choose the ideal peer from peerList
     * The weight is inversely proportional to both latency and load.
     * @param peerList = list of all peers having the data
     * @param port = port of the running peer
     * @param loadFromAllPeers
     * @return returns the port of the ideal peer
     */
    public static Pair findIdealPeer(List<Integer> peerList, int port, HashMap<Integer, Integer> loadFromAllPeers) {
        //TODO : NULL checks to see if things are not breaking.
        int min=MAX_VALUE;
        File file = new File("/Users/bhaargavsriraman/Desktop/UMN/Git/xfs/src/main/java/latency.properties");
        FileInputStream fileInputStream = null;
        int idealPeer=-1;
        try {
            int latency = 0;
            fileInputStream = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(fileInputStream);
            for(int peer:peerList){
                if(peer != port){
                    System.out.println("Peer: " +peer);
                    System.out.println("Port: " +port);
                    System.out.println(prop.getProperty(port+"."+peer));
                    latency = Integer.parseInt(prop.getProperty(port+"."+peer));
                    int weight = loadFromAllPeers.get(peer)*latency;
                    if(weight < min){
                        min = weight;
                        idealPeer=peer;
                    }
                }
            }
            return new Pair(idealPeer,latency);
        } catch (FileNotFoundException e) {
            System.out.println("Error: Couldn't locate the properties file");
        } catch (IOException e) {
            System.out.println("Error: Couldn't read the properties file");
        }

        return new Pair(-1,0);
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
            return DatatypeConverter.printHexBinary(digest);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * Checks if the file provided already exist with the peer
     * @param fName: Name of the file
     * @param port: Port of the host peer
     * @return: if the file is present or not.
     */
    public static boolean doesFileExist(String fName, int port) {
        List<String> listOfFiles = new ArrayList<>();
        File folder = new File("/Users/bhaargavsriraman/Desktop/UMN/Git/xfs/src/main/java/" + port);
        File[] fileNames = folder.listFiles();
        if(fileNames != null){
            for(File f : fileNames) {
                if(f.getName().equalsIgnoreCase(fName))
                    return true;
            }
        }
        return false;
    }
}
