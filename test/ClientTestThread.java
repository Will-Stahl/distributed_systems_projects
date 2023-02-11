import java.util.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * class is a thread that has String value
 * which is set is run() so that it can be checked when thread ends
 */
class ClientTestThread extends Thread {
    final private static int MAXSTRING = 120;
    private volatile String article;
    private int _port;

    public ClientTestThread(int port) {
        _port = port;
    }
    // run() based off ProcessClientResponseFromServer() from PubSubClient.java
    @Override
    public void run() {
        byte[] serverResponse = new byte[MAXSTRING];
        try{
            DatagramSocket socket = new DatagramSocket(_port);
            DatagramPacket packet = new DatagramPacket(serverResponse, serverResponse.length);
            System.out.println("before receiving packet");
            socket.receive(packet);  // block here
            System.out.println("After receiving packet");
            article = new String(packet.getData(), 0, packet.getLength());
        } catch (Exception e){
            System.out.println("[CLIENT]: Socket error occured. Please restart Client. Exiting...");
            article = "";
        }
    }

    public String getArticle() {
        return article;
    }
}