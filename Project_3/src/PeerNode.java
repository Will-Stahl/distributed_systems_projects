import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class PeerNode extends UnicastRemoteObject implements PeerNodeInterface {
    public PeerNode() throws RemoteException {}
    
    public int GetLoad() throws RemoteException {
        return 0;
    }

    public String Download(String fname) throws RemoteException {
        return "";
    }
}