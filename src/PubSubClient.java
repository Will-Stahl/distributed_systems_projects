import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;

public class PubSubClient {
    final int PORT_NUMBER = 1099;

    public void receivePacketsFromServer(PubSubServer server, String article){
        try{
            DatagramPacket packet = new DatagramPacket(new byte[256], 256);
            DatagramSocket socket = new DatagramSocket(PORT_NUMBER);
            
            socket.receive(packet);
            String packetData = new String(packet.getData(), 0, packet.getLength());
            System.out.println(packetData);
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        try{
            String hostName = args[0];
            Registry registry = LocateRegistry.getRegistry(hostName);
            PubSubServer server = (PubSubServer) registry.lookup("server.PubSubServer");
            
            //server.join()

        } catch (Exception e){
            System.err.println("Client Exception: " + e.toString());
        }
    }
}
