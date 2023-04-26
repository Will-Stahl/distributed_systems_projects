import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface PeerNodeInterface extends Remote {
    public int GetLoad() throws RemoteException;

    public FileDownload Download(String fname, int peerID)
        throws RemoteException;

    public void Ping() throws RemoteException;
}