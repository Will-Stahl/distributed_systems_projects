import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public class BulletinBoardClient {
    private ArrayList<BulletinBoardServer> joinedServers;
    private ArrayList<String> articles;
    private int clientPort;
    private String IP;

    public BulletinBoardClient(InetAddress IP) {
        joinedServers = new ArrayList<>();
        articles = new ArrayList<>();
        clientPort = SetRandomClientPortNumber();
        this.IP = IP.getHostAddress();
    }

    // Function for getting a random port number
    private static int SetRandomClientPortNumber(){
        Random rand = new Random();
        return (rand.nextInt((65535 - 1024) + 1)) + 1024;
    }

    /**
     * Function for displaying menu options
     **/
    private static void DisplayOptions(){
        System.out.println("Ports currently available to join: 2000, 2001, 2002, 2003 and 2004");
        System.out.println("1. Enter \"Join\" to join the group server.");
        System.out.println("2. Enter \"Leave\" to leave the group server.");
        System.out.println("3. Enter \"Post\" to post an article.");
        System.out.println("4. Enter \"Read\" to read a list of articles.");
        System.out.println("5. Enter \"Choose\" to choose one of the articles and display its contents.");
        System.out.println("6. Enter \"Reply\" to reply to an existing article (also posts a new article).");
        System.out.println("7. Enter \"Display\" to display all articles currently available.");
    }

    public String GetAndValidateClientRequest(){
        Scanner sc = new Scanner(System.in);
        String clientRequest = "";
        while (true){
            System.out.println("\n[CLIENT]: Enter command: ");
            clientRequest = sc.nextLine();
            String lowerCaseRequest = clientRequest.trim().toLowerCase();

            if (lowerCaseRequest.startsWith("display")){
                // TODO: Add code for displaying available articles
                //System.out.println("");
                //break;
            }

            // If we have a join or leave essage, then we can simply break from the loop
            if ((lowerCaseRequest.startsWith("join") ||  
                lowerCaseRequest.startsWith("leave"))){
                    if (ValidJoinOrLeaveRequestFormat(lowerCaseRequest)) break;
            } else if (ValidReplyRequest(lowerCaseRequest) || ValidChooseRequest(lowerCaseRequest) || 
                        ValidReadRequest(lowerCaseRequest) ||ValidPostRequest(lowerCaseRequest)) {
                break;
            } else if (lowerCaseRequest.startsWith("post:")){
                System.out.println("[CLIENT]: Invalid Post format. Please use \"Post: <Article Title>;<Article Contents>\"");
            } else if (lowerCaseRequest.startsWith("read:")){
                System.out.println("[CLIENT]: Invalid Read format. Please use \"Read: <List of Article IDs separated by commas>\"");
            } else if (lowerCaseRequest.startsWith("choose:")){
                System.out.println("[CLIENT]: Invalid Choose format. Please use \"Choose: <Article ID>\"");
            } else if (lowerCaseRequest.startsWith("reply:")){
                System.out.println("[CLIENT]: Invalid Choose format. Please use \"Reply: <Article ID>;<Article Title>;<Article Contents>\"");
            } else{
                System.out.println("\nOnly the following 7 operations can be performed by the client:");
                DisplayOptions();
            }
        }
        return clientRequest.trim();
    }

    // TODO: validate command line requests for all 4 functions
    private boolean ValidReplyRequest(String lowerCaseRequest) {
        return false;
    }

    private boolean ValidChooseRequest(String lowerCaseRequest) {
        return false;
    }

    private boolean ValidReadRequest(String lowerCaseRequest) {
        return false;
    }

    private boolean ValidPostRequest(String lowerCaseRequest) {
        return false;
    }

    private static boolean ValidJoinOrLeaveRequestFormat(String lowerCaseRequest){
        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            System.out.println("[CLIENT]: Join or Leave commands can only be formatted like \"join: <port number>\" or \"leave: <port number>\"");
            return false;
        }

        try{
            int serverPort = Integer.parseInt(parts[1].trim());
            // If port is invalid, then print error message and exit.
            if (!CheckValidPort(serverPort)){
                System.out.println("\n[CLIENT]: Port number specified is invalid. Valid port numbers are 2000, 2001, 2002, 2003 and 2004");
                return false;
            }
            return true;
        } catch (Exception e){
            System.out.println("[CLIENT]: Server port should be an integer value such as 2000, 2001, 2002, 2003 or 2004");
            return false;
        }
    }

    /**
     * Function for making RMI calls to the group server depending on the client's request.
     * @param server - Server object for making RMI calls
     * @param address - Client address
     */
    public void SendClientRequestToServer(String hostName, InetAddress address){
        String clientRequest = GetAndValidateClientRequest();
        try{
            if (clientRequest.startsWith("join") || clientRequest.startsWith("leave")){
                HandleJoinOrLeaveRequests(hostName, IP, clientPort, clientRequest);
            } else {
                // TODO: Get server object depending on consistency strategy
                if (clientRequest.startsWith("post")){
                    // TODO: Call server post function
                } else if (clientRequest.startsWith("read")){
                    // TODO: Call server read function
                } else if (clientRequest.startsWith("choose")){
                    // TODO: Call server choose function
                } else if (clientRequest.startsWith("reply")){
                    // TODO: Call server reply function
                }
            } 
        } catch (RemoteException e){
            System.out.println("[CLIENT]: No response from server since it is offline. Try joining another server!");
        }
    }

    private static void HandleJoinOrLeaveRequests(String hostName, String IP, int clientPort, String clientRequest) throws RemoteException{
        String[] parts = clientRequest.split(":");
        String command = parts[0].toLowerCase();
        int serverPort = Integer.parseInt(parts[1].trim());
        
        BulletinBoardServerInterface server = ConnectToServer(hostName, serverPort);

        if (server == null){
            System.out.printf("[CLIENT]: Server at port %d has not been started yet and cannot be connected to.\n", serverPort);
            return;
        }

        if (command.equals("join")){
            boolean join = server.Join(IP, clientPort);
            if (join){
                System.out.printf("[CLIENT]: Client at port %d successfully joined server at port %d.\n", clientPort, server.GetServerPort());
            } else {
                System.out.println("[CLIENT]: It's possible that server capacity has been reached or the IP address provided is invalid or the client is already part of the server.");
            }
        } else {
            boolean leave = server.Leave(IP, clientPort);
            if (leave){
                System.out.printf("[CLIENT]: Client at port %d successfully left server at port %d.\n", clientPort, server.GetServerPort());
            } else {
                System.out.printf("[CLIENT]: This client is not currently part of the server at port %d\n", server.GetServerPort());
            }
        }
    }

    private static BulletinBoardServerInterface ConnectToServer(String hostName, int serverPort){
        // Fixed set of ports are mapped to specific server numbers
        HashMap<Integer, Integer> portToServerMap = new HashMap<>();
        portToServerMap.put(2000, 1);
        portToServerMap.put(2001, 2);
        portToServerMap.put(2002, 3);
        portToServerMap.put(2003, 4);
        portToServerMap.put(2004, 5);

        try{
            int serverNumber = portToServerMap.get(serverPort);
            Registry registry = LocateRegistry.getRegistry(hostName, serverPort);
            BulletinBoardServerInterface server = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_" + serverNumber);
            System.out.println("\n[CLIENT]: Registry lookup was successful!");
            return server;
        } catch (Exception e){
            System.out.println("\n[CLIENT]: Error occurred while looking up the registry name.");
        }
        return null;
    }

    private static boolean CheckValidPort(int port){
        Set<Integer> ports = new HashSet<>();
        ports.add(2000);
        ports.add(2001);
        ports.add(2002);
        ports.add(2003);
        ports.add(2004);

        return ports.contains(port);
    }

    public static void main(String[] args){
        if (args.length != 1){
            System.out.println("\n[SERVER]: Usage: java BulletinBoardClient <hostname>");
            System.out.println("[CLIENT]: Exiting...");
            System.exit(0);
        }

        String hostName = args[0];
        try{
            InetAddress address = InetAddress.getByName(hostName);
            BulletinBoardClient client = new BulletinBoardClient(address);
            // Thread for sending client requests to server
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(true){
                        client.SendClientRequestToServer(args[0], address);
                    }
                }
            }).start();
        } catch (Exception e){
            System.out.println("[CLIENT]: Error occurred. Exiting...");
            System.exit(0);
        }
    }
}