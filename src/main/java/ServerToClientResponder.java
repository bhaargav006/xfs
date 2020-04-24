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
                //All the serverhelper functions will be used here
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
