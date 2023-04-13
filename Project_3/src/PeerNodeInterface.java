import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface PeerNodeInterface extends Remote {
    public int GetLoad() throws RemoteException;

    public String Download(String fname) throws RemoteException;
}