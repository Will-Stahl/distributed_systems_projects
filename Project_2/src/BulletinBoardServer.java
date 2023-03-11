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
public class BulletinBoardServer extends UnicastRemoteObject
implements BulletinBoardServerInterface /*, ServerToServerInterface*/ {
    
    private Integer serverPort; 
    private Integer serverNum;
    private static ArrayList<BulletinBoardClient> clients = new ArrayList<>();
    private static ArrayList<String> articles = new ArrayList<>();
    private final int MAX_CLIENTS = 5;
    private static int clientCount = 0;

    // P2P data structures
    private int coordNum;
    private int coordPort;
    private String coordHost;
    private int nextID;
    //private ConsistencyStrategy cStrat;  // initialize to specifc strategy
    //private ReferencedTree contentTree;  // article tree data structure

    public BulletinBoardServer(int serverPort, int serverNum,
        String consistency) throws RemoteException{
        this.serverPort = serverPort;
        this.serverNum = serverNum;
        nextID = 1;
        /* 
        contentTree = new ReferencedTree();
        
        // TODO: intialize article tree, maybe find coordinator, initialize consitency strategy object
        if (consistency.equals("sequential")) {
            cStrat = new SequentialStrategy();
        }
        else if (consistency.equals("quorum")) {

        }
        else if (consistency.equals("readyourwrites")) {

        }
        else {
            System.out.println("Invalid strategy entered, defaulting to sequential");
            cStrat = new SequentialStrategy();
        }*/
    }

    public int GetServerPort() throws RemoteException{
        return serverPort;
    }

    public int GetServerNumber(){
        return serverNum;
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

        // check for valid IP address
        if (IsValidIPAddress(IP)){
            // TODO: Add clients
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
        return false;
    }

    /**
     * @param article content to publish
     * if primary server, uses strategy (non-remote method)
     * if not, uses RMI to contact primary
     
    public boolean Publish(String article) throws RemoteException {
        int replyTo = 0;  // publishing replies to root
        if (coordNum == serverNum) {  // this server is the coordinator
            // increment nextID after call
            return cStrat.ServerPublish(nextID++, article, serverPort,
                                        serverNum, contentTree);
        }
        try{
            // else look up coordinator in registry, request it to publish
            Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
            ServerToServerInterface coordServer = (ServerToServerInterface)
            registry.lookup("BulletinBoardServer_" + coordNum);
            //return coordServer.CoordinatorPost(article, replyTo);
            return true;
        } catch (Exception e){
            return false;
        }
    }
    */

    /**
     * @param article content to publish
     * @param replyTo ID of article to reply to
     * if primary server, uses strategy (non-remote method)
     * if not, uses RMI to contact primary
     
    public boolean Reply(String article, int replyTo) throws RemoteException {
        replyTo = 0;  // publishing replies to root
        if (coordNum == serverNum) {  // this server is the coordinator
            // increment nextID after call
            return cStrat.ServerPublish(nextID++, article, serverPort,
                                        serverNum, contentTree);
        }
        // else look up coordinator in registry, request it to publish
        try{
            Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
            ServerToServerInterface coordServer = (ServerToServerInterface)
            registry.lookup("BulletinBoardServer_" + coordNum);
            //return coordServer.CoordinatorPost(article, replyTo);
            return true;
        } catch (Exception e){
            return false;
        }
        
        
    }
    */

    /**
     * returns indented string previewing all articles with ID
     * for now, we delegate viewing details to client
     
    public boolean Read() throws RemoteException {
        if (serverNum == coordNum) {  // if this server is the coordinator
            //return cStrat.ServerRead();
            return true;
        }

        try{
            // else look up coordinator in registry, request it to publish
            Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
            ServerToServerInterface coordServer = (ServerToServerInterface)
            registry.lookup("BulletinBoardServer_" + coordNum);
            return coordServer.CoordinatorRead();
        } catch (Exception e){
            return false;
        }
    }
    */

    /**
     * @param articleID ID of article requested in full
     
    public boolean Choose(int articleID) throws RemoteException {
        if (coordNum == serverNum) {
            //return cStrat.ServerChoose(articleID, contentTree);
            return true;
        }

        try{
            // else look up coordinator in registry, request it to publish
            Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
            ServerToServerInterface coordServer = (ServerToServerInterface)
            registry.lookup("BulletinBoardServer_" + coordNum);
            return coordServer.CoordinatorChoose(articleID);
        } catch (Exception e){
            return false;
        }
    }
    */

    /**
     * from ServerToServerInterface
     * @param article content to publish
     * @param replyTo ID of article to reply to
     * this server should be the coordinator
     * calls ServerPublish() using strategy object
     
    public boolean CoordinatorPost(String article, int replyTo)
            throws RemoteException {
        return cStrat.ServerPublish(article, serverPort, nextID++);
    } */

    /**
     * from ServerToServerInterface
     * should be called by coordinator on non-coordinators
     * @param newID unique ID generated by coordinator
     * @param article string consisting of article
     * @param replyTo specify article to reply to 
     * if 0, it replies to root, which is just a new post
     
    public boolean UpdateTree(int newID, String article, int replyTo)
                                throws RemoteException {
        //return contentTree.AddNode(newID, article, replyTo);
        return true;
    } */

    /**
     * should only be called on coordinator be non-coordinator
     * coordinator uses strategy to return article preview based
     * on chosen consistency
     
    public String CoordinatorRead() throws RemoteException {
        // TODO: just call the consistency strategy onject, maybe control for some errors
        return "";
    } 

    public boolean CoordinatorRead() throws RemoteException {
        // TODO: just call the consistency strategy onject, maybe control for some errors
        return false;
    } */

    /**
     * from ServerToServerInterface
     * should only be called on coordinator server object
     * @param articleID ID if article to return in full
     * returns message if article isn't found
     
    public String CoordinatorChoose(int articleID) throws RemoteException {
        cStrat.ServerChoose(articleID);
    } 

    public boolean CoordinatorPost(String article) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'CoordinatorPost'");
    }

    public boolean CoordinatorChoose(int articleID) throws RemoteException {
        //cStrat.ServerChoose(articleID);
        return false;
    } */

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
        // If no argument is specified, then print error message and exit
        if (args.length != 2){
            System.out.println("\n[SERVER]: Usage: java BulletinBoardServer <port> <consistency>");
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
        } catch(Exception e) {
            System.out.println("\n[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
}