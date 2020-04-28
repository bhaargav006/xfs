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
    static int currentLoad;
    public Client(int port, int currentLoad) {
        //Start a Sync thread which sends update to Server periodically if the file list changes in Client
        FilesSyncThread filesSyncThread = new FilesSyncThread(port);
        filesSyncThread.start();
        // Starts a separate thread to listen to requests
        this.currentLoad = currentLoad;
        ClientThread clientThread = new ClientThread(port);
        clientThread.start();
    }

    public static void main(String[] args) {
        //Start the client Threads
        new Client(Integer.parseInt(args[0]), currentLoad);
        try {
            SocketConnection trackingServerSocket = new SocketConnection(8000);
            Scanner in = new Scanner(System.in);
            while(true) {
                System.out.println("********Welcome to XFS--Peer To Peer FileSystem********");
                System.out.println();
                System.out.println("[F] Find \n[D] Download File \n[E] Exit \n");
                String input = in.nextLine();
                switch (input.charAt(0)) {
                    case 'F':
                        System.out.println("Enter filename:");
                        String fname = in.nextLine();
                        ClientHelper.sendFindRequest(trackingServerSocket,fname);
                        break;
                    case 'D':
                        System.out.println("Enter filename:");
                        String downloadFilename = in.nextLine();
                        ClientHelper.processMessageFromClient(trackingServerSocket, downloadFilename);
                        break;
                    case 'E':
                        break;

                }
            }
        } catch (IOException e) {
            System.out.println("Tracking server is down.");;
        }

    }
}
