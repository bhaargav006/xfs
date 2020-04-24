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

    ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket client = null;
                try {
                    client = serverSocket.accept();
                    SocketConnection sc = new SocketConnection(client);
                    Thread clientResponder = new ClientToClientResponder(sc);
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
}
