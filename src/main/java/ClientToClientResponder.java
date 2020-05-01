import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * Handler for incoming requests from other clients (peers)
 */
public class ClientToClientResponder extends  Thread{

    SocketConnection client;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    int myPort;
    public ClientToClientResponder(SocketConnection sc, int myPort) {
        this.client = sc;
        this.oos = sc.getOos();
        this.ois = sc.getOis();
        this.myPort = myPort;

    }

    @Override
    public void run() {
        try{
//            while(true) {
                //process the messages from client
                String msg = (String)ois.readObject();
                System.out.println("In C2C responder: " + msg);
                ClientHelper.processMessage(client, msg, myPort);
//            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
