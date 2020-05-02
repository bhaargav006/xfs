import java.io.IOException;

public class ServerHealth extends Thread {

    boolean done = false;
    @Override
    public void run() {
        while(!done) {
            try {
                SocketConnection connection = new SocketConnection(8000);
                connection.getOos().writeUTF("Ping");
                String reply = connection.getOis().readUTF();
                if(reply.equals("OK"))
                {
                    done = true;
                    Client.serverStatus = true;

                }
                connection.close();
            } catch (IOException e) {
                //Retrying
            }

        }
    }
}
