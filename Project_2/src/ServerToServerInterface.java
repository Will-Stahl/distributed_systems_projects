import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerToServerInterface extends Remote
{
    public boolean UpdateTree(String article /* TODO: parameters are undecided*/) throws RemoteException;

    /**
     * Replica contacts coordinator with this method, asking it to post
     */
    public boolean CoordinatorPost(String article) throws RemoteException;
    // methods for updating article structure
    // method also for reading, as the coordinator will read from multiple in quorum mode
}