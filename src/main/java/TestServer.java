public class TestServer extends Thread {
    int port;
    public TestServer(int port) {
        this.port = port;
    }
    @Override
    public void run() {
        new TrackingServer(port);
    }
}
