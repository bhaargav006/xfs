import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

/***
 * Driver function of client
 * Server is assumed at 8000 port
 */
public class Client {
    static ServerSocket clientSocket;
    static Socket clientAsServer;
    static volatile AtomicInteger currentLoad;
    static boolean serverStatus;
    public Client(int port, AtomicInteger currentLoad) {
        //Start a Sync thread which sends update to Server periodically if the file list changes in Client
        FilesSyncThread filesSyncThread = new FilesSyncThread(port);
        filesSyncThread.start();
        // Starts a separate thread to listen to requests
        Client.currentLoad = currentLoad;
        ClientThread clientThread = new ClientThread(port);
        clientThread.start();
    }

    public static void main(String[] args) {
        new Client(Integer.parseInt(args[0]), new AtomicInteger(0));
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
                    ClientHelper.sendFindRequest(fName);
                    break;
                }
                case 'D': {
                    System.out.println("Enter filename:");
                    String fName = in.nextLine();
                    System.out.println("Start Time: " +System.currentTimeMillis());
                    if(!ClientHelper.doesFileExist(fName, Integer.parseInt(args[0]))) {
                        ClientHelper.DownloadFileFromPeers(fName, Integer.parseInt(args[0]));
                    }
                    else System.out.println("File already exists with the current peer.");
                    System.out.println("End Time: " +System.currentTimeMillis());
                    break;
                }
                case 'E':
                    System.out.println("Thank you. Stay safe!");
                    exit(0);
            }
        }
    }
}
