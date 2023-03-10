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

// TODO: implement second interface for P2P communication
public class BulletinBoardServer extends UnicastRemoteObject
implements BulletinBoardServerInterface, ServerToServerInterface {
    
    private Integer serverPort; 
    private Integer serverNum;

    // P2P data structures
    private Integer coordNum;
    private Integer coordPort;
    private String coordHost;
    private Integer nextID;
    private ConsistencyStrategy cStrat;
    // TODO: article tree data structure

    public BulletinBoardServer(Integer serverPort, Integer serverNum,
        String consistency) throws RemoteException{
        this.serverPort = serverPort;
        this.serverNum = serverNum;
        nextID = 1;
        
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
        }
    }

    public Integer GetServerPort(){
        return serverPort;
    }

    public Integer GetServerNumber(){
        return serverNum;
    }

    public boolean Join(String IP, int Port) throws RemoteException
    {
        if (Port > 65535 || Port < 0) {
            System.out.println("[SERVER]: Client port number is invalid and cannot be used for communication.");
            return false;
        }

        // check for valid IP address
        if (IsValidIPAddress(IP)){
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
     * if primary server, use strategy (non-remote method)
     * if not, use RMI to contact primary
     * this RM should call a non-remote method as a strategy
     * strategy consists of contacting other servers
     */
    public boolean Publish(String article) throws RemoteException {
        if (coordNum == serverNum) {  // this server is the coordinator
            return cStrat.ServerPublish(article, serverPort, nextID++);
        }
        // TODO: else look up coordinator in registry, request it to publish
        Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
        ServerToServerInterface coordServer = (ServerToServerInterface)
            registry.lookup("BulletinBoardServer_" + coordNum);
        return coordServer.CoordinatorPost(article);
    }

    /**
     * from ServerToServerInterface
     * this server should be the coordinator
     * calls ServerPublish() using strategy object
     */
    public boolean CoordinatorPost(String article) throws RemoteException {
        return cStrat.ServerPublish(article, serverPort, nextID++);
    }

    /**
     * from ServerToServerinterface
     * specify article to reply to 
     * if 0, it replies to root, which is just a new post
     */
    public boolean UpdateTree(String article, Integer newID, Integer replyTo) {
        // TODO: call ReferencedTree
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
        // If no argument is specified, then print error message and exit
        if (args.length != 1){
            System.out.println("\n[SERVER]: Usage: java BulletinBoardServer <port>");
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
            System.out.println("[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
}