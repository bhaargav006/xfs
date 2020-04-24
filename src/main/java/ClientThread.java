import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/***
 * Client Thread to receive continuous requests from other peers (Clients)
 */
public class ClientThread extends Thread {
    int port;

    public ClientThread(int port) {
        this.port = port;
    }

    ServerSocket clientSocket;

    @Override
    public void run() {
        try {
            clientSocket = new ServerSocket(port);
            while (true) {
                Socket peers = null;
                try {
                    peers = clientSocket.accept();
                    SocketConnection sc = new SocketConnection(peers);
                    Thread clientResponder = new ClientToClientResponder(sc);
                    clientResponder.start();
                } catch (IOException e) {
                    peers.close();
                    System.out.println("Error in accepting request from other peers.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
