import java.io.ObjectInputStream;

/***
 * Handler for incoming requests from peers/Clients to Tracking Server
 */
public class ServerToClientResponder extends Thread {

    SocketConnection client;
    public ServerToClientResponder(SocketConnection sc) {
        client = sc;
    }
    public void run() {
        try{
            ObjectInputStream ois = client.getOis();
            System.out.println("I am here!");
            String msg = (String) ois.readObject();
            ServerHelper.processMessage(client, msg);
        }
        catch (Exception e) {
            System.out.println("One of the clients disconnected.");
        }
    }
}
