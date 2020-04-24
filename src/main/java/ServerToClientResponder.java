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
            while(true) {
                //process the messages from client
                ObjectInputStream ois = client.getOis();
                String msg = ois.readUTF();
                ServerHelper.processMessage(client, msg);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
