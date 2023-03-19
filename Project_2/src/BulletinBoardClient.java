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
    private static ArrayList<BulletinBoardServerInterface> joinedServers;
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
                if (ValidJoinOrLeaveRequestFormat(lowerCaseRequest)){
                    break;
                }
            } else if (lowerCaseRequest.startsWith("post")){
                if (ValidPostRequest(lowerCaseRequest)) break;
            } else if (lowerCaseRequest.startsWith("read")){
                if (ValidReadRequest(lowerCaseRequest)) break;
            } else if (lowerCaseRequest.startsWith("choose")){
                if (ValidChooseRequest(lowerCaseRequest)) break;
            } else if (lowerCaseRequest.startsWith("reply")){
                if (ValidReplyRequest(lowerCaseRequest)) break;
            } else{
                System.out.println("\nOnly the following 7 operations can be performed by the client:");
                DisplayOptions();
            }
        }
        return clientRequest.trim();
    }

    private boolean ValidPostRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.startsWith("post:")){
            System.out.println("[CLIENT]:  Colon missing. Please use \"Post: <Article Title>;<Article Contents>\"");
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            System.out.println("[CLIENT]: Article details are missing. Please use \"Post: <Article Title>;<Article Contents>\"");
            return false;
        }

        String articleString = parts[1].trim();
        if (!articleString.contains(";")){
            System.out.println("[CLIENT]: Semicolon missing between title and contents. Article format should be \"<Article Title>;<Article Contents>\"");
            return false;
        }

        String[] articleParts = articleString.split(";");
        if (articleParts.length != 2){
            System.out.println("[CLIENT]: Either title or contents are missing. Article format should be \"<Article Title>;<Article Contents>\"");
            return false;
        }

        return true;
    }

    private boolean ValidReadRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.startsWith("read:")){
            System.out.println("[CLIENT]:  Colon missing. Please use \"Read: <List of Article IDs separated by commas>\"");
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            System.out.println("[CLIENT]: Article IDs are missing. Please use \"Read: <List of Article IDs separated by commas>\"");
            return false;
        }

        // Check if only commas are used as separaters
        if (!parts[1].trim().contains(",")){
            System.out.println("[CLIENT]: Commas are missing. Please use \"Read: <List of Article IDs separated by commas>\"");
            return false;
        }

        
        // Ensure that every ID format is "ID" followed by a number
        String[] IDs = parts[1].trim().split(",");
        for (String ID : IDs){
            ID = ID.trim();
            System.out.println(ID);
            if (!ID.startsWith("id")){
                System.out.println("[CLIENT]: ID format must begin with \"ID\". Example: \"ID1\", \"ID2\" and so on.");
                return false;
            }

            String articleNumber = ID.substring(2, ID.length());
            if (!articleNumber.matches("\\d+")){
                System.out.println("[CLIENT]: Article ID has to have a number. Example: \"ID1\", \"ID2\" and so on.");
                return false;
            }
        }

        return true;
    }

    private boolean ValidReplyRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.startsWith("reply:")){
            System.out.println("[CLIENT]:  Colon missing. Please use \"Reply: <Article ID>;<Reply>\"");
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            System.out.println("[CLIENT]: Invalid Reply format. Please use \"Reply: <Article ID>;<Reply>\"");
            return false;
        }

        String replyString = parts[1].trim();
        if (!replyString.contains(";")){
            System.out.println("[CLIENT]: Semicolon missing between ID and reply. Please use \"Reply: <Article ID>;<Reply>\"");
            return false;
        }

        String[] replyParts = replyString.split(";");
        if (replyParts.length != 2){
            System.out.println("[CLIENT]: Article ID or reply are missing. Please use \"Reply: <Article ID>;<Reply>\"");
            return false;
        }

        String replyID = replyParts[0].trim();
        if (!replyID.startsWith("id")){
            System.out.println("[CLIENT]: ID format must begin with \"ID\". Example: \"ID1\", \"ID2\" and so on.");
            return false;
        }

        String articleNumber = replyID.substring(2, replyID.length());
        if (!articleNumber.matches("\\d+")){
            System.out.println("[CLIENT]: Article ID has to have a number. Example: \"ID1\", \"ID2\" and so on.");
            return false;
        }

        return true;
    }

    private boolean ValidChooseRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.startsWith("choose:")){
            System.out.println("[CLIENT]:  Colon missing. Please use \"Reply: <Article ID>;<Reply>\"");
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            System.out.println("[CLIENT]: Invalid Choose format. Please use \"Choose: <Article ID>\"");
            return false;
        }

        String articleID = parts[1].trim();
        if (!articleID.startsWith("id")){
            System.out.println("[CLIENT]: ID format must begin with \"ID\". Example: \"ID1\", \"ID2\" and so on.");
            return false;
        }

        String articleNumber = articleID.substring(2, articleID.length());
        if (!articleNumber.matches("\\d+")){
            System.out.println("[CLIENT]: Article ID has to have a number. Example: \"ID1\", \"ID2\" and so on.");
            return false;
        }

        return true;
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
                // If no servers have been joined yet, then we cant call post, read, choose or reply
                if (joinedServers.size() == 0){
                    System.out.println("[CLIENT]: Please join a server before attempting to post, read, reply or choose!");
                    return;
                }

                // Just get the first server for now
                BulletinBoardServerInterface server = joinedServers.get(0);

                if (clientRequest.startsWith("post:")){
                    String[] parts = clientRequest.split(":");
                    //server.Publish(parts[1].trim());
                } else if (clientRequest.startsWith("read:")){
                    //server.Read();
                } else if (clientRequest.startsWith("choose:")){
                    //server.Choose(clientPort);
                } else if (clientRequest.startsWith("reply:")){
                    //server.Reply(clientRequest, clientPort);
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
                joinedServers.add(server);
            } else {
                System.out.println("[CLIENT]: It's possible that server capacity has been reached or the IP address provided is invalid or the client is already part of the server.");
            }
        } else {
            boolean leave = server.Leave(IP, clientPort);
            if (leave){
                System.out.printf("[CLIENT]: Client at port %d successfully left server at port %d.\n", clientPort, server.GetServerPort());
                joinedServers.remove(server);
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