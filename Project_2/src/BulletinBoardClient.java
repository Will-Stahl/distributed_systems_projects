import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class BulletinBoardClient {

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
        if (args.length != 2){
            System.out.println("\n[SERVER]: Usage: java BulletinBoardClient <hostname> <server_port>");
            System.out.println("[CLIENT]: Exiting...");
            System.exit(0);
        }

        int serverPort = Integer.parseInt(args[1]);
        // If port is invalid, then print error message and exit.
        if (!CheckValidPort(serverPort)){
            System.out.println("\n[CLIENT]: Port number specified is invalid. Valid port numbers are 2000, 2001, 2002, 2003 and 2004");
            System.out.println("[CLIENT]: Exiting...");
            System.exit(0);
        }

        String hostName = args[0];

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
        } catch (Exception e){
            System.out.println("\n[CLIENT]: Error occurred while looking up the registry name. Make sure the server is running on the respective port before running the client script.");
            System.out.println("[CLIENT]: Exiting...");
            System.exit(0);
        }
        
    }
}
