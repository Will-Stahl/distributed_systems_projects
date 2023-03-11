import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerToServerInterface extends Remote
{
    public boolean UpdateTree(int ID, String article, int replyTo) throws RemoteException;

    /**
     * Replica contacts coordinator with this method, asking it to post
     */
    public boolean CoordinatorPost(String article) throws RemoteException;
    
    public boolean CoordinatorRead() throws RemoteException;
    public boolean CoordinatorChoose(int articleID) throws RemoteException;
}