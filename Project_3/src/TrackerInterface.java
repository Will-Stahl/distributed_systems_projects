import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface TrackerInterface extends Remote {
    // Client joins server using this function
    public boolean Join(String IP, int Port, int machID) throws RemoteException;

    // TODO: Signature probably needs to change
    public boolean Leave(int machID) throws RemoteException;

    // return list of nodes that contain argument file
    public FileInfo Find(String fname) throws RemoteException;

    // peer node calls this on tracker to send its updated list
    public boolean UpdateList(List<FileInfo> fnames, int machID) throws RemoteException;

    // Peer pings server to make sure its live. This function doesn't return anything since if a server is offline,
    // then trying to call this function using the remote server object simply raises an Exception that is handled
    // on the peer side.
    public void Ping() throws RemoteException;
}