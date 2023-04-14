import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Tracker extends UnicastRemoteObject implements TrackerInterface {
    private static int serverPort = 8000;
    // TODO: data structure to store joined peers
    private static ArrayList<TrackedPeer> peerInfo;
    // known files and their locations, must be checked when peer unreachable!
    private static HashMap<String, ArrayList> fileMap;

    public Tracker() throws RemoteException {}
    
    public boolean Join(String IP, int Port, int machID) throws RemoteException {
        // TODO: Distinguish between clients using machID and don't add duplicate clients
        System.out.printf("[SERVER]: Added peer node at IP: %s and Port: %d.\n", IP, Port);
        return true;
    }

    public boolean Leave(String IP, int Port) throws RemoteException {
        return true;
    }

    public ArrayList<TrackedPeer> Find(String fname) throws RemoteException {
        ArrayList ids;
        if (ids = fileMap.get(fname) == null) {  // no attached peer has it
            return null;
        }
        ArrayList<TrackedPeer> answer = new ArrayList<TrackedPeer>();
        for (int nodeID : ids) {
            answer.add(peerInfo.get())
        }
        return null;
    }

    public boolean UpdateList(ArrayList<String> fnames) throws RemoteException {
        return false;
    }

    private void removeNode(int machID) {
        // TODO: remove from both data structures
        // remove file from hashmap if no ids attached
    }

    public static void main(String[] args){
        try{
            TrackerInterface server = new Tracker();
            Registry registry = LocateRegistry.createRegistry(serverPort);
            registry.rebind("TrackingServer", server);
            System.out.printf("\n[SERVER]: Tracking Server is ready at port %d. \n", serverPort);
        } catch (Exception e){
            System.out.println("\n[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
    
}