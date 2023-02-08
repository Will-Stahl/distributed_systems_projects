import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.util.*;

public class PubSubClient {
    final static int PORT_NUMBER = 1099;

    private static String getAndValidateClientRequest(){
        Set<String> validRequestType = new HashSet<String>();
        validRequestType.add("join");
        validRequestType.add("leave");
        validRequestType.add("publish");
        validRequestType.add("subscribe");
        validRequestType.add("unsubscribe");
        validRequestType.add("ping");

        Scanner sc = new Scanner(System.in);
        String clientRequest = "";
        while (true){
            System.out.println("Enter your request to the server:");
            clientRequest = sc.nextLine();
            String[] words = clientRequest.trim().split(" ");
            if(validRequestType.contains(words[0].toLowerCase())) break;
            System.out.println("Error in request! Requests must start with one of the following operations (not case sensitive): Join, Leave, Publish, Subscribe, Unsubscribe, Ping");
        }
        sc.close();
        return clientRequest.trim();
    }

    private static void sendClientRequestToServer(DatagramSocket socket, InetAddress address){
        try{
            String clientRequest = getAndValidateClientRequest();
            byte[] requestBuffer = clientRequest.getBytes();
            DatagramPacket packet = new DatagramPacket(requestBuffer, requestBuffer.length, address, PORT_NUMBER);
            socket.send(packet);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String processClientResponseFromServer(DatagramSocket socket, InetAddress address){
        String response = "";
        try{
            byte[] serverResponse = new byte[1024];
            DatagramPacket packet = new DatagramPacket(serverResponse, serverResponse.length);
            socket.receive(packet);
            response = new String(packet.getData(), 0, packet.getLength());
            return response;
        } catch (IOException e){
            e.printStackTrace();
        }
        return response;
    }

    public static void main(String[] args) throws IOException{
        try{
            String hostName = args[0];
            Registry registry = LocateRegistry.getRegistry(hostName);
            PubSubServer server = (PubSubServer) registry.lookup("server.PubSubServer");
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(hostName);

            // Thread for sending client requests to server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while (true){
                        sendClientRequestToServer(socket, address);
                    }
                }
            }).start();

            // Thread for receiving subscribed articles back from the server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while (true){
                        String response = processClientResponseFromServer(socket, address);
                        System.out.println("Response from server: " + response);
                    }
                }
            }).start();
        } catch (Exception e){
            System.err.println("Client Exception: " + e.toString());
        }
    }
}
