import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerToServerInterface extends Remote
{
    public boolean UpdateTree(String article) throws RemoteException;
    // methods for updating article structure
    // method also for reading, as the coordinator will read from multiple in quorum mode
}