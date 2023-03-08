import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class BulletinBoardClient {
    private Set<Integer> ports;

    public BulletinBoardClient(){
        ports = new HashSet<>();
    }

    private Set<Integer> GetPorts(){
        return ports;
    }

    private void SetPorts(){
        ports.add(2000);
        ports.add(2001);
        ports.add(2002);
        ports.add(2003);
        ports.add(2004);
    }

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("[CLIENT]: Port number is missing");
        }

        BulletinBoardClient client = new BulletinBoardClient();
        client.SetPorts();
        Set<Integer> ports = client.GetPorts();

        if (!ports.contains(Integer.parseInt(args[0]))){
            System.out.println("[CLIENT]: The port number requested is not a valid port number. You can only join ports 2000, 2001, 2002, 2003 and 2004");
        }
        
    }
}
