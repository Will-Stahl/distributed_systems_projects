import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface TrackerInterface extends Remote {
    // Client joins server using this function
    public boolean Join(String IP, int Port, int machID) throws RemoteException;

    // TODO: Signature probably needs to change
    public boolean Leave(String IP, int Port) throws RemoteException;

    // return list of nodes that contain argument file
    public ArrayList<TrackedPeer> Find(String fname) throws RemoteException;

    // peer node calls this on tracker to send its updated list
    public boolean UpdateList(ArrayList<String> fnames) throws RemoteException;
}