import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.*;

// TODO: simulate network delays, probably via wrapper/decorator class
// TODO: save contentTree with java serialization
public class BulletinBoardServer extends UnicastRemoteObject
implements BulletinBoardServerInterface, ServerToServerInterface {
    
    private int serverPort; 
    private int serverNum;
    private static ArrayList<ClientInfo> clients = new ArrayList<>();
    private ArrayList<BulletinBoardServerInterface> serverList;
    private final int MAX_CLIENTS = 5;
    private static int clientCount = 0;

    // P2P data structures
    private int coordNum;
    private int coordPort;
    private String coordHost;
    private int nextID;
    private ConsistencyStrategy cStrat;  // initialize to specifc strategy
    private ReferencedTree contentTree;  // article tree data structure
    private String serverHost;

    public BulletinBoardServer(int serverPort, int serverNum,
        String consistency) throws RemoteException{
        this.serverPort = serverPort;
        this.serverNum = serverNum;
        coordNum = 5;  // coordinator hard-chosen as highest number for now
        nextID = 1;
        contentTree = new ReferencedTree();
        coordPort = 2004;
        coordHost = "localhost";
        serverHost = "localhost";
        serverList = new ArrayList<>();
        
        if (consistency.equals("sequential")) {
            cStrat = new SequentialStrategy();
        }
        // else if (consistency.equals("quorum")) {

        // }
        // else if (consistency.equals("readyourwrites")) {

        // }
        else {
            System.out.println("Invalid strategy entered, defaulting to sequential");
            cStrat = new SequentialStrategy();
        }
    }

    public boolean Join(String IP, int Port) throws RemoteException
    {
        if (Port > 65535 || Port < 0) {
            System.out.println("[SERVER]: Client port number is invalid and cannot be used for communication.");
            return false;
        }

        // If server is at max capacity, then prompt user to try joining again at a later time.
        if (clientCount == MAX_CLIENTS) {

            System.out.println("[SERVER]: Server Capacity reached! Please try joining again later.");
            return false;
        }

        // If client has been added earlier, then don't add them again.
        for (int i = 0; i < clients.size(); i++) {
            ClientInfo cl = clients.get(i);
            if (cl.GetIP().equals(IP) && cl.GetPort() == Port) {
                System.out.println("[SERVER]: Client is already part of the group server.");
                return false;
            }
        }

        // check for valid IP address
        if (IsValidIPAddress(IP)){
            clients.add(new ClientInfo(IP, Port));
            System.out.printf("\n[SERVER]: Added new client with IP: %s, Port: %d\n", IP, Port);
            return true;
        }
        System.out.println("[SERVER]: Invalid IP Address");
        return false;
    }

    private static boolean IsValidIPAddress(String IP) {
        String[] parts = IP.split("\\.");

        if (parts.length != 4) return false;

        for (int i = 0; i < parts.length; i++){
            String part = parts[i];
            if (Integer.parseInt(part) < 0 || Integer.parseInt(part) > 255) return false;
        }
        return true;
    }
    
    public boolean Leave(String IP, int Port) throws RemoteException{
        // check for subscriber in Subscribers
        ClientInfo clientPtr = null;
        for (int i = 0; i < clients.size(); i++) {
            ClientInfo Sub = clients.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                clientPtr = clients.get(i);
                clients.remove(i);
            }
        }
        if (clientPtr == null) {
            System.out.printf("[SERVER]: Client at IP Address %s is and Port %d is not currently part of the server.\n", IP, Port);
            return false;
        }
        
        clientCount -= 1;
        System.out.printf("[SERVER]: Removed client at address %s and Port %d.\n", IP, Port);
        return true;
    }

    /**
     * @param article content to publish
     * calls strategy
     */
    public boolean Publish(String article) throws RemoteException {
        return cStrat.ServerPublish(article, 0, this);
    }

    /**
     * @param article content to publish
     * @param replyTo ID of article to reply to
     * if primary server, uses strategy (non-remote method)
     * if not, uses RMI to contact primary
    */
    public boolean Reply(String article, int replyTo) throws RemoteException {
        // same as call to ServerPublish() in Publish(), but with replyTo
        return cStrat.ServerPublish(article, replyTo, this);
    }

    /**
     * returns indented string previewing all articles with ID
     * for now, we delegate viewing details to client
    */
    public String Read() throws RemoteException {
        return cStrat.ServerRead(this);
    }


    /**
     * @param articleID ID of article requested in full
    */
    public String Choose(int articleID) throws RemoteException {
        return cStrat.ServerChoose(articleID, contentTree);
    }


    /**
     * from ServerToServerInterface
     * @param article content to publish
     * @param replyTo ID of article to reply
     * this server should be the coordinator if this method is called on it
     * calls ServerPublish() using strategy object
     */
    public boolean CoordinatorPost(String article, int replyTo) throws RemoteException {
        try {
            return cStrat.ServerPublish(article, replyTo, this);
        } catch (Exception e) {
            return false;
        } 
    }

    /**
     * from ServerToServerInterface
     * should be called by coordinator on non-coordinators
     * @param newID unique ID generated by coordinator
     * @param article string consisting of article
     * @param replyTo specify article to reply to 
     * if 0, it replies to root, which is just a new post
     */
    public boolean UpdateTree(int newID, String article, int replyTo)
                                throws RemoteException {
        try {
            return contentTree.AddNode(newID, article, replyTo);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * from ServerToServerInterface
     * should only be called on coordinator server object
     * @param articleID ID if article to return in full
     * returns message if article isn't found
     */
    public String CoordinatorChoose(int articleID) throws RemoteException {
        return cStrat.ServerChoose(articleID, contentTree);
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

    public void IncrementID() {
        nextID++;
    }

    // =============== getters/setters ==================
    public String GetCoordHost() {
        return coordHost;
    }

    public int GetCoordPort() {
        return coordPort;
    }

    public int GetCoordNum() {
        return coordNum;
    }

    public int GetServerNumber() {  // as in self server number
        return serverNum;
    }

    public int GetServerPort() {
        return serverPort;
    }

    public String GetServerHost(){
        return serverHost;
    }

    public ReferencedTree GetTree() {
        return contentTree;  // allow strategies to manipulate tree
    }

    public int GetCurrID() {
        return nextID;
    }

    public ArrayList<BulletinBoardServerInterface> GetServerList(){
        return serverList;
    }

    public void AddToServerList(BulletinBoardServerInterface server){
        serverList.add(server);
    }

    public static void main(String[] args){
        // TODO: Add server hostname as parameter
        // If no argument is specified, then print error message and exit
        if (args.length != 2){
            System.out.println("\n[SERVER]: Usage: java BulletinBoardServer <hostname> <port> <consistency>");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);

        // If port is invalid, then print error message and exit.
        if (!CheckValidPort(port)){
            System.out.println("\n[SERVER]: Port number specified is invalid. Valid port numbers are 2000, 2001, 2002, 2003 and 2004");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }

        // Fixed set of ports are mapped to specific server numbers
        HashMap<Integer, Integer> portToServerMap = new HashMap<>();
        portToServerMap.put(2000, 1);
        portToServerMap.put(2001, 2);
        portToServerMap.put(2002, 3);
        portToServerMap.put(2003, 4);
        portToServerMap.put(2004, 5);

        try{
            int serverNum = portToServerMap.get(port);
            BulletinBoardServerInterface server = new BulletinBoardServer(port, serverNum, args[1]);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("BulletinBoardServer_" + serverNum, server);
            System.out.printf("\n[SERVER]: Bulletin Board Server %d is ready at port %d. \n", serverNum, port);

            // Connect to central server if this is a replica
            if (serverNum != 5){
                try{
                    registry = LocateRegistry.getRegistry("localhost", 2004);
                    ServerToServerInterface coord = (ServerToServerInterface)
                                registry.lookup(
                                "BulletinBoardServer_" + 5);
                    coord.AddToServerList(server);
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("[SERVER]: Please start the coordinator server first.");
                    System.exit(0);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();  // DEBUG
            System.out.println("\n[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
}