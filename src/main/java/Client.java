import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/***
 * Driver function of client
 */
public class Client {
    ServerSocket serverSocket;

    public Client(int port) {
        // Starts a separate thread to listen to requests

        ClientThread clientThread = new ClientThread(port);
        clientThread.start();
    }

    public static void main(String[] args) {
        Client client = new Client(8001);
        try {
            SocketConnection trackingServerSocket = new SocketConnection(8000);
            List<String> listOfFiles = ClientHelper.getListOfFiles(8001);
            ClientHelper.sendFileSystemContent(trackingServerSocket.getOos(),8001,listOfFiles);

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

                        break;
                    case 'D':
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
