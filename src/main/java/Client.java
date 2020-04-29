import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/***
 * Driver function of client
 */
public class Client {
    static ServerSocket clientSocket;
    static Socket clientAsServer;
    static volatile int currentLoad;
    public Client(int port, int currentLoad) {
        //Start a Sync thread which sends update to Server periodically if the file list changes in Client

        // Starts a separate thread to listen to requests
        this.currentLoad = currentLoad;
        ClientThread clientThread = new ClientThread(port);
        clientThread.start();
    }

    public static void main(String[] args) {
        //Shouldn't the currentLoad here be 0?
        Client client = new Client(Integer.parseInt(args[0]), currentLoad);
        try {
            SocketConnection trackingServerSocket = new SocketConnection(8000);

            List<String> listOfFiles = ClientHelper.getListOfFiles(Integer.parseInt(args[0]));
            ClientHelper.sendFileSystemContent(trackingServerSocket.getOos(), Integer.parseInt(args[0]),listOfFiles);
            Scanner in = new Scanner(System.in);
            while(true) {
                System.out.println("********Welcome to XFS--Peer To Peer FileSystem********");
                System.out.println();
                System.out.println("[F] Find \n[D] Download File \n[E] Exit \n");
                String input = in.nextLine();
                switch (input.charAt(0)) {
                    case 'F': {
                        System.out.println("Enter filename:");
                        String fName = in.nextLine();
                        System.out.println(ClientHelper.sendFindRequest(trackingServerSocket, fName));
                        break;
                    }
                    case 'D': {
                        System.out.println("Enter filename:");
                        String fName = in.nextLine();
                        ClientHelper.processMessageFromClient(trackingServerSocket, fName);
                        ArrayList<Integer> peerList = (ArrayList<Integer>)ClientHelper.sendFindRequest(trackingServerSocket, fName);
                        while(!peerList.isEmpty()){
                            int idealPeer = ClientHelper.findIdealPeer(peerList);
                        }
                        break;
                    }
                    case 'E':

                }
            }
        } catch (IOException e) {
            System.out.println("Tracking server is down.");;
        }

    }
}
