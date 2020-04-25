import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Driver for Tracking Server
 */
public class TrackingServer {

    // this map contains a set of ports (peers) that store a file
    static volatile ConcurrentHashMap<String, Set<Integer>> listOfFileOwners;
    ServerSocket serverSocket;

    // the server listens to requests from clients and handles them using ServerToClientResponder
    public TrackingServer(int port) {
        listOfFileOwners = new ConcurrentHashMap<String, Set<Integer>>();
        try {
            serverSocket = new ServerSocket(port);
            while(true) {
                Socket client = null;
                try {
                    client = serverSocket.accept();
                    SocketConnection sc = new SocketConnection(client);
                    Thread clientResponder = new ServerToClientResponder(sc);
                    clientResponder.start();
                } catch (IOException e) {
                    client.close();
                    System.out.println("Error in the server sockets while accepting peers");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TrackingServer ts = new TrackingServer(8000);
    }
}
