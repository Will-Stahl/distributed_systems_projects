import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface PeerNodeInterface extends Remote {
    public int GetLoad() throws RemoteException;

    // TODO: Can probably leave this as a private static function, but not sure
    //public String Download(String fname) throws RemoteException;
}