import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Tracker extends UnicastRemoteObject implements TrackerInterface {
    public Tracker() throws RemoteException {}

    public ArrayList Find(String fname) throws RemoteException {
        return null;
    }

    public boolean UpdateList(ArrayList<String> fnames) throws RemoteException {
        return false;
    }
}