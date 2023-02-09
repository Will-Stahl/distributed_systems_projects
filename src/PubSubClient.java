import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.util.*;

public class PubSubClient {
    final static int PORT_NUMBER = 1099;

    private static void PrintWelcomeMessage(){
        System.out.println("Before entering your request, please see the following rules for the 6 operations that can be performed (none of these are case sensitive): ");
        System.out.println("Command for joining a server: Join");
        System.out.println("Command for leaving a server: Leave");
        System.out.println("Command for publishing to a server: \"Publish: <Article Name>\". For example: \"Publish: Sports;UMN;;contents\" ");
        System.out.println("Command for subscribing to an article: \"Subscribe: <Article Name>\". For example: \"Subscribe: Sports;UMN;;\"");
        System.out.println("Command for unsubscribing from an article: \"Unsubscribe: <Article Name>\". For example: \"Unsubscribe: Sports;UMN;;\"");
    }

    private static String GetAndValidateClientRequest(){
        Scanner sc = new Scanner(System.in);
        String clientRequest = "";
        PrintWelcomeMessage();
        while (true){
            clientRequest = sc.nextLine();
            clientRequest = clientRequest.trim().toLowerCase();

            // If we have a join, leave or ping message, then we can simply break from the loop
            if (clientRequest.startsWith("join") || clientRequest.startsWith("leave") || clientRequest.startsWith("ping")) break;

            // If the publish, subscribe or unsubscribe command format is valid, then break; otherwise, ask for a correctly formatted input from the client(s).
            if ((ValidPublishSubOrUnSubCommandFormat(clientRequest))) break;
            else if (clientRequest.startsWith("publish:")) System.out.println("Invalid Publish Command format. Please use \"Publish: <Article Name>\"");
            else if (clientRequest.startsWith("subscribe:")) System.out.println("Invalid Subscribe Command format. Please use \"Subscribe: <Article Name>\"");
            else if (clientRequest.startsWith("unsubscribe:")) System.out.println("Invalid Unsubscribe Command format. Please use \"Unsubscribe: <Article Name>\"");
            else System.out.println("Error in request! Requests must start with one of the following operations (not case sensitive): Join, Leave, Publish, Subscribe, Unsubscribe, Ping");
        }
        sc.close();
        return clientRequest.trim();
    }

    private static boolean ValidPublishSubOrUnSubCommandFormat(String clientRequest){
        String [] words = clientRequest.split(":");
        return (words.length == 2) && (words[0] == "publish:" || words[0] == "subscribe:" || words[0] == "unsubscribe:");
    }

    private static void SendClientRequestToServer(PubSubServerInterface server, InetAddress address){
        String IP = address.getHostAddress();
        try{
            String clientRequest = GetAndValidateClientRequest();
            if (clientRequest.startsWith("join")){
                server.Join(IP, PORT_NUMBER);
            } else if (clientRequest.startsWith("leave")){
                server.Leave(IP, PORT_NUMBER);
            } else if (clientRequest.startsWith("publish:")){
                String[] words = clientRequest.split(":");
                server.Publish(words[1].trim(), IP, PORT_NUMBER);
            } else if (clientRequest.startsWith("subscribe:")){
                String[] words = clientRequest.split(":");
                server.Subscribe(IP, PORT_NUMBER, words[1].trim());
            } else if (clientRequest.startsWith("unsubscribe:")){
                String[] words = clientRequest.split(":");
                server.Unsubscribe(IP, PORT_NUMBER, words[1].trim());
            } else if (clientRequest.startsWith("ping")){
                server.Ping();
            }
        } catch (RemoteException e){
            e.printStackTrace();
        }
        
    }

    private static void ProcessClientResponseFromServer(DatagramSocket socket, InetAddress address){
        try{
            byte[] serverResponse = new byte[1024];
            DatagramPacket packet = new DatagramPacket(serverResponse, serverResponse.length);
            socket.receive(packet);
            String response = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Response from server: " + response);
        } catch (IOException e){
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
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(hostName);
            
            // Thread for sending client requests to server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while (true){
                        SendClientRequestToServer(server, address);
                    }
                }
            }).start();

            // Thread for receiving subscribed articles back from the server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while (true){
                        ProcessClientResponseFromServer(socket, address);
                    }
                }
            }).start();
        } catch (Exception e){
            System.err.println("Client Exception: " + e.toString());
        }
    }
}
