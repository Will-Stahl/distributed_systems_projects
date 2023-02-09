import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.util.*;

public class PubSubClient {
    final static int PORT_NUMBER = 8888;
    private static DatagramSocket socket;

    private static void PrintWelcomeMessage(){
        System.out.println("\nWelcome to the PubSub Client!");
        System.out.println("Before entering your request, please see the following rules for the 6 operations that can be performed (none of these are case sensitive): \n");
        System.out.println("1. Enter \"Join\" to join the group server");
        System.out.println("2. Enter \"Leave\" to leave the group server");
        System.out.println("3. Enter \"Publish\" to send a new article");
        System.out.println("4. Enter \"Subscribe\" to request a subscription to the group server");
        System.out.println("5. Enter \"Unsubscribe\" to request an unsubscribe to the group server");
    }

    private static String GetAndValidateClientRequest(){
        Scanner sc = new Scanner(System.in);
        String clientRequest = "";
        while (true){
            System.out.println("\nEnter command: ");
            clientRequest = sc.nextLine();
            String lowerCaseRequest = clientRequest.trim().toLowerCase();

            // If we have a join, leave or ping message, then we can simply break from the loop
            if (lowerCaseRequest.startsWith("join") || 
                lowerCaseRequest.startsWith("leave") || 
                lowerCaseRequest.startsWith("ping")){
                    break;
                }

            // If the publish, subscribe or unsubscribe command format is valid, then break; otherwise, ask for a correctly formatted input from the client(s).
            if (ValidPublishSubOrUnSubCommandFormat(lowerCaseRequest)) {
                break;
            } else if (lowerCaseRequest.startsWith("publish:")) {
                System.out.println("Invalid Publish Command format. Please use \"Publish: <Article Name>\"");
            } else if (lowerCaseRequest.startsWith("subscribe:")) {
                System.out.println("Invalid Subscribe Command format. Please use \"Subscribe: <Article Name>\"");
            } else if (lowerCaseRequest.startsWith("unsubscribe:")) {
                System.out.println("Invalid Unsubscribe Command format. Please use \"Unsubscribe: <Article Name>\"");
            } else {
                System.out.println("Error in request! Requests must start with one of the following operations (not case sensitive): Join, Leave, Publish, Subscribe, Unsubscribe, Ping");
            }
        }
        return clientRequest.trim();
    }

    private static boolean ValidPublishSubOrUnSubCommandFormat(String clientRequest){
        String [] words = clientRequest.split(":");
        return (words.length == 2) && (words[0].equals("publish") || 
                                        words[0].equals("subscribe") || 
                                        words[0].equals("unsubscribe"));
    }

    private static void SendClientRequestToServer(PubSubServerInterface server, InetAddress address){
        String IP = address.getHostAddress();
        String clientRequest = GetAndValidateClientRequest();
        String lowerCaseRequest = clientRequest.toLowerCase();
        try{
            if (lowerCaseRequest.startsWith("join")){
                server.Join(IP, PORT_NUMBER);
            } else if (lowerCaseRequest.startsWith("leave")){
                server.Leave(IP, PORT_NUMBER);
            } else if (lowerCaseRequest.startsWith("publish:")){
                String[] words = clientRequest.split(":");
                server.Publish(words[1].trim(), IP, PORT_NUMBER);
            } else if (lowerCaseRequest.startsWith("subscribe:")){
                String[] words = clientRequest.split(":");
                server.Subscribe(IP, PORT_NUMBER, words[1].trim());
            } else if (lowerCaseRequest.startsWith("unsubscribe:")){
                String[] words = clientRequest.split(":");
                server.Unsubscribe(IP, PORT_NUMBER, words[1].trim());
            } else if (lowerCaseRequest.startsWith("ping")){
                server.Ping();
            }
        } catch (RemoteException e){
            e.printStackTrace();
        }
        
    }

    private static void ProcessClientResponseFromServer(InetAddress address){
        byte[] serverResponse = new byte[1024];
        try{
            socket = new DatagramSocket(PORT_NUMBER);
            DatagramPacket packet = new DatagramPacket(serverResponse, serverResponse.length);
            while(true){
                socket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Response from server: " + response);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        try{
            if (args.length < 1) {
                System.out.print("Please provide a hostname as an argument.\n");
                return;
            }
            String hostName = args[0];
            Registry registry = LocateRegistry.getRegistry(hostName);
            PubSubServerInterface server = (PubSubServerInterface) registry.lookup("server.PubSubServer");
            
            InetAddress address = InetAddress.getByName(hostName);
            PrintWelcomeMessage();

            // Thread for receiving subscribed articles back from the server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(true){
                        ProcessClientResponseFromServer(address);
                    }
                }
            }).start();

            // Thread for sending client requests to server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(true){
                        SendClientRequestToServer(server, address);
                    }
                }
            }).start();

        } catch (Exception e){
            System.err.println("Client Exception: " + e.toString());
        }
    }
}
