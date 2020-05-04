import java.util.concurrent.atomic.AtomicInteger;

public class TestClient extends Thread {

    int port;
    String op;
    String fn;
    public TestClient(int port, String op, String fn) {
        this.port = port;
        this.op = op;
        this.fn = fn;
    }
    @Override
    public void run() {
        Client client = new Client(port, new AtomicInteger(0));
        if(op.equals("D"))
            ClientHelper.DownloadFileFromPeers(fn,port);
    }
}

