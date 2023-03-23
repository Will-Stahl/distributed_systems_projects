import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface BulletinBoardServerInterface extends Remote
{
    public int GetServerPort() throws RemoteException;
    public void AddToServerList(BulletinBoardServerInterface server) throws RemoteException;
    public List<BulletinBoardServerInterface> GetWriteQuorum() throws RemoteException;
    public List<BulletinBoardServerInterface> GetReadQuorum() throws RemoteException;
    public void SetQuorums(int NR, int NW) throws RemoteException;
    public boolean Ping() throws RemoteException;
    public boolean PingCoordinator() throws RemoteException;
    public int GetCurrID() throws RemoteException;
    public void SetTree(ReferencedTree tree) throws RemoteException;
    public void IncrementID() throws RemoteException;
    public int GetServerNumber() throws RemoteException;
    public String GetServerHost() throws RemoteException;
    public ReferencedTree GetTree() throws RemoteException;
    public boolean UpdateTree(int newID, String article, int replyTo) throws RemoteException;
    public ArrayList<BulletinBoardServerInterface> GetServerList() throws RemoteException;
    public boolean Join(String IP, int Port) throws RemoteException;
    public boolean Leave(String IP, int Port) throws RemoteException;
    public boolean Publish(String article) throws RemoteException;
    public String Read() throws RemoteException;
    public String Choose(int articleID) throws RemoteException;
    public boolean Reply(String article, int replyTo) throws RemoteException;
}
