import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.util.*;

public class PubSubClient {
    private static int port;
    private static DatagramSocket socket;
    final private static int MAXSTRING = 120;
    private static Set<String> subscribedArticles = new HashSet<>();;

    // Function for getting a random port number
    private static void SetRandomPortNumber(){
        Random rand = new Random();
        port = (rand.nextInt((65535 - 1024) + 1)) + 1024;
    }

    /**
     * Function for printing a Welcome Message
     **/
    private static void PrintWelcomeMessage(){
        System.out.println("\nWelcome to the PubSub Client!");
        System.out.println("Before entering your request, please see the following rules for the 6 operations that can be performed (none of these are case sensitive): \n");
        DisplayOptions();
    }

    /**
     * Function for displaying menu options
     **/
    private static void DisplayOptions(){
        System.out.println("1. Enter \"Join\" to join the group server.");
        System.out.println("2. Enter \"Leave\" to leave the group server.");
        System.out.println("3. Enter \"Publish\" to send a new article.");
        System.out.println("4. Enter \"Subscribe\" to request a subscription to the group server.");
        System.out.println("5. Enter \"Unsubscribe\" to request an unsubscribe to the group server.");
        System.out.println("6: Enter \"Display\" to display published articles.");
    }

    /** 
     * Function for displaying articles that have been published to the client
    */
    private static void DisplaySubscribedArticles(){
        System.out.println("[CLIENT]: The following articles have been published to this Client:");
        Iterator<String> it = subscribedArticles.iterator();
        int count = 1;
        while (it.hasNext()) {
            System.out.printf("[CLIENT]: Article #%d: %s\n", count, it.next());
            count += 1;
        }
    }

    /**
     * Function for taking user input from the terminal for performing join, leave, 
     * publish, subscribe, unsubscribe and display operations
     */
    private static String GetAndValidateClientRequest(){
        Scanner sc = new Scanner(System.in);
        String clientRequest = "";
        while (true){
            System.out.println("\n[CLIENT]: Enter command: ");
            clientRequest = sc.nextLine();
            String lowerCaseRequest = clientRequest.trim().toLowerCase();

            // Display all articles that have been published to the client if they exist.
            if (lowerCaseRequest.startsWith("display")) {
                if (subscribedArticles.size() == 0){
                    System.out.println("[CLIENT]: Client is not currently subscribed to any articles.");
                }
                else{
                    DisplaySubscribedArticles();
                }
                break;
            }

            // If we have a join or leave essage, then we can simply break from the loop
            if (lowerCaseRequest.startsWith("join") ||  lowerCaseRequest.startsWith("leave")){
                break;
            }

            // If the publish, subscribe or unsubscribe command format is valid, then break; otherwise, ask for a correctly formatted input from the client(s).
            if (ValidPublishSubOrUnSubCommandFormat(lowerCaseRequest)) {
                break;
            } else if (lowerCaseRequest.startsWith("publish:")) {
                System.out.println("[CLIENT]: Invalid Publish Command format. Please use \"Publish: <Article Name>\"");
            } else if (lowerCaseRequest.startsWith("subscribe:")) {
                System.out.println("[CLIENT]: Invalid Subscribe Command format. Please use \"Subscribe: <Article Name>\"");
            } else if (lowerCaseRequest.startsWith("unsubscribe:")) {
                System.out.println("[CLIENT]: Invalid Unsubscribe Command format. Please use \"Unsubscribe: <Article Name>\"");
            } else {
                System.out.println("\nOnly the following 6 operations can be performed by the client:");
                DisplayOptions();
            }
        }
        return clientRequest.trim();
    }

    /**
     * Function for validating if the publish, subscribe and unsubscribe functions have a colon
     * and two parts to them, for example: "publish: Sports;;;contents" is valid, whereas 
     * "publish Sports;;;contents is invalid".
     * @param clientRequest: The client request that is input from the terminal
     * @return
     */
    private static boolean ValidPublishSubOrUnSubCommandFormat(String clientRequest){
        String [] words = clientRequest.split(":");
        return (words.length == 2) && (words[0].equals("publish") || 
                                        words[0].equals("subscribe") || 
                                        words[0].equals("unsubscribe"));
    }

    /**
     * Function for making RMI calls to the group server depending on the client's request.
     * @param server - Server object for making RMI calls
     * @param address - Client address
     */
    private static void SendClientRequestToServer(PubSubServerInterface server, InetAddress address){
        String IP = address.getHostAddress();
        String clientRequest = GetAndValidateClientRequest();
        String lowerCaseRequest = clientRequest.toLowerCase();
        try{
            if (lowerCaseRequest.startsWith("join")){
                server.Join(IP, port);
            } else if (lowerCaseRequest.startsWith("leave")){
                server.Leave(IP, port);
            } else if (lowerCaseRequest.startsWith("publish:")){
                String[] words = clientRequest.split(":");
                server.Publish(words[1].trim(), IP, port);
            } else if (lowerCaseRequest.startsWith("subscribe:")){
                String[] words = clientRequest.split(":");
                server.Subscribe(IP, port, words[1].trim());
            } else if (lowerCaseRequest.startsWith("unsubscribe:")){
                String[] words = clientRequest.split(":");
                server.Unsubscribe(IP, port, words[1].trim());
            }
        } catch (RemoteException e){
            System.out.println("[CLIENT]: No response from server since it is offline. Exiting...");
            System.exit(0);
        }
        
    }

    /**
     * Function for receiving published messages from the server via UDP.
     * @param address - Client address
     */
    private static void ProcessClientResponseFromServer(InetAddress address){
        byte[] serverResponse = new byte[MAXSTRING];
        try{
            socket = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(serverResponse, serverResponse.length);
            while(true){
                socket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println("[CLIENT]: Response from server: " + response);
                subscribedArticles.add(response);
            }
        } catch (Exception e){
            System.out.println("[CLIENT]: Socket error occured. Please restart Client. Exiting...");
            System.exit(0);
        }
    }

    public static void main(String[] args) throws IOException{
        try{
            if (args.length < 1) {
                System.out.println("[CLIENT]: Please provide a hostname as an argument.");
                return;
            }
            SetRandomPortNumber();
            String hostName = args[0];
            PubSubServerInterface server = (PubSubServerInterface) Naming.lookup("rmi://"+hostName+"/server.PubSubServer");

            // Periodically ping server to check if it is live.
            Timer timer = new Timer();
            timer.schedule(new PingServer(server), 0, 10000);
            
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
            System.err.println("[CLIENT]: Client Exception Occurred. Please restart client! Exiting now...");
        }
    }
}

// Class for pinging the RMI group server every 10 seconds.
class PingServer extends TimerTask{
    private PubSubServerInterface server;

    PingServer(PubSubServerInterface server){
        this.server = server;
    }

    public void run(){
        try{
            server.Ping();
        } catch (Exception e){
            System.out.println("[CLIENT]: Server is offline! Exiting...");
            System.exit(0);
        }
    }
}
