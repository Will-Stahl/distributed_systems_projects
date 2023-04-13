import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface TrackerInterface extends Remote {
    // return list of nodes that contain argument file
    public ArrayList Find(String fname) throws RemoteException;

    // peer node calls this on tracker to send its updated list
    public boolean UpdateList(ArrayList<String> fnames) throws RemoteException;
}