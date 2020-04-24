import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * Handler for incoming requests from other clients (peers)
 */
public class ClientToClientResponder extends  Thread{

    SocketConnection client;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    public ClientToClientResponder(SocketConnection sc) {
        this.client = sc;
        this.oos = sc.getOos();
        this.ois = sc.getOis();
    }

    @Override
    public void run() {
        try{
            while(true) {
                //process the messages from client
                String msg = ois.readUTF();
                ClientHelper.processMessage(client, msg);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
