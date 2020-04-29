import java.io.IOException;
import java.util.List;

public class FilesSyncThread extends Thread {
    int port;
    public FilesSyncThread(int port) {
        this.port = port;
    }
    @Override
    public void run() {
        while(true) {
            try {
                SocketConnection trackingServer = new SocketConnection(8000);
                List<String> listOfFiles = ClientHelper.getListOfFiles(port);
                ClientHelper.sendFileSystemContent(trackingServer.getOos(), port,listOfFiles);
                trackingServer.close();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Exception in FileSystem Thread");
            } catch (IOException e) {
                System.out.println("Server Not found...Retrying...");
            }
        }
    }
}
