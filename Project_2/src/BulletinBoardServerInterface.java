import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoardServerInterface extends Remote
{
    public int GetServerPort() throws RemoteException;
    public boolean Join(String IP, int Port) throws RemoteException;
    public boolean Leave(String IP, int Port) throws RemoteException;

    // TODO?: add parameters to ensure that client has joined?
    /* 
    public boolean Publish(String article) throws RemoteException;
    public boolean Read() throws RemoteException;
    public boolean Choose(int articleID) throws RemoteException;
    public boolean Reply(String article, int replyTo) throws RemoteException;
    */
}
