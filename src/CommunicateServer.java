import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;

public class CommunicateServer extends UnicastRemoteObject
implements Communicate
{
    // TODO: data structures to store client info and contents

    public CommunicateServer() throws RemoteException
    {

    }

    public boolean Join(String IP, int Port) throws RemoteException
    {
        return false;
    }
    
    public boolean Leave(String IP, int Port) throws RemoteException
    {
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
        return false;
    }

    public static void main(String args[]) throws RemoteException
    {
        LocateRegistry.createRegistry(1099);
        Communicate content_srv = new CommunicateServer();
        Naming.rebind("server.Communicate", content_srv);
        System.out.println("Publish-Subscribe Server is ready.");
    }
}