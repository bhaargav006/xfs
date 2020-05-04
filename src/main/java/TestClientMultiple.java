public class TestClientMultiple {
    public static void main(String[] args) throws InterruptedException {
        TestServer t = new TestServer(8000);
        t.start();
        TestClient t1 = new TestClient(8001, "F","hello1.txt");
        t1.start();
        TestClient t2 = new TestClient(8002,"F","hello1.txt");
        t2.start();
        TestClient t3 = new TestClient(8003,"D","hello1.txt");
        t3.start();
        TestClient t4 = new TestClient(8004,"D","hello1.txt");
        t4.start();
        TestClient t5 = new TestClient(8005,"D","hello1.txt");
        t5.start();

        Thread.sleep(15000);
        System.exit(-1);
    }
}
