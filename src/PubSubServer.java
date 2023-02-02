import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;

public class PubSubServer extends UnicastRemoteObject
implements PubSubServerInterface
{
    // TODO: data structures to store client info and contents
    private ArrayList<SubscriberInfo> Subscribers;

    public PubSubServer() throws RemoteException
    {
        Subscribers = new ArrayList<SubscriberInfo>();
    }

    /**
     * Checks if IP address and port number are valid
     * Also checks if client already has Joined
     *
     * @param IP client IP address
     * @param Port client listening at port number
     * @return boolean indicating success
     */
    public boolean Join(String IP, int Port) throws RemoteException
    {
        if (Port > 65535 || Port < 0) {
            System.out.print("Client sent invalid port number\n");
            return false;
        }

        for (int i = 0; i < Subscribers.size(); i++) {
            SubscriberInfo Sub = Subscribers.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                System.out.print("Client already joined\n");
                return false;
            }
        }

        // TODO: check for valid IP address

        Subscribers.add(new SubscriberInfo(IP, Port));
        System.out.printf("Added new client with IP: %s, Port: %d\n", IP, Port);
        return true;
    }

    /**
     * Subscriber should always call Leave() before it terminates
     * Removes calling subscriber from SubscriberInfo list
     *
     * @param IP client IP address
     * @param Port client listening at port number
     * @return boolean indicating success
     */
    public boolean Leave(String IP, int Port) throws RemoteException
    {
        for (int i = 0; i < Subscribers.size(); i++) {
            SubscriberInfo Sub = Subscribers.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                Subscribers.remove(i);
                System.out.print("Removed subscriber\n");
                return true;
            }
        }
        System.out.print("Client was not already joined\n");
        return false;
    }
    
    public boolean Subscribe(String IP, int Port, String Article)
    throws RemoteException
    {
        return false;
    }
    
    public boolean Unsubscribe(String IP, int Port, String Article)
    throws RemoteException
    {
        return false;
    }
    
    public boolean Publish(String Article, String IP, int Port)
    throws RemoteException
    {
        return false;
    }
    
    public boolean Ping() throws RemoteException
    {
        return true;
    }

    public static void main(String args[])
    throws RemoteException, MalformedURLException
    {
        LocateRegistry.createRegistry(1099);
        PubSubServerInterface ContentSrv = new PubSubServer();
        Naming.rebind("server.PubSubServer", ContentSrv);
        System.out.println("Publish-Subscribe Server is ready.");
    }

    private boolean CheckValidIP(String IP) {
        // String[] Parts = IP.split("\.");
        return false;
    }
}