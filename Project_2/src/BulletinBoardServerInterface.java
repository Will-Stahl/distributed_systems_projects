import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface BulletinBoardServerInterface extends Remote
{
    public boolean Join(String IP, int Port) throws RemoteException;
    public boolean Leave(String IP, int Port) throws RemoteException;
}
