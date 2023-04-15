import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Tracker extends UnicastRemoteObject implements TrackerInterface {
    private static int serverPort = 8000;

    // data structure to store joined peers
    private static ArrayList<TrackedPeer> peerInfo;
    // known files and their locations
    private static HashMap<String, HashSet<Integer>> fileMap;

    public Tracker() throws RemoteException {
        fileMap = new HashMap<String, HashSet<Integer>>();
        peerInfo = new ArrayList<TrackedPeer>();
        for (int i = 0; i < 5; i ++){
            peerInfo.add(null);
        }
    }

    /**
     * Gets peer info tracked in tracker process
     * @param IP ip address of peer
     * @param Port port of peer process
     * @param machID peer's assigned unique machine ID
     */
    public boolean Join(String IP, int Port, int machID) throws RemoteException {
        if (peerInfo.get(machID) == null){
            peerInfo.set(machID, new TrackedPeer(machID, Port, IP));
            System.out.printf("[SERVER]: Added peer node at IP: %s and Port: %d.\n", IP, Port);
            return true;
        } else {
            System.out.printf("[SERVER]: Process with ID %d has already joined.\n", machID); 
            return false; 
        }
    }

    public boolean Leave(int machID) throws RemoteException {
        if (peerInfo.get(machID) == null){
            System.out.printf("[SERVER]: Process with ID %d was not joined.\n", machID); 
        return false;  // node wasn't joined
        } else {
            removeNode(machID);
            System.out.printf("[SERVER]: Removed node with ID: %d\n", machID);
            return true;
        }
    }

    /**
     * returns list of clients that have the requested file
     * null if tracker has no knowledge of file
     * does NOT check if peer is joined, as this function does not affect state
     * @param fname filename peer is seeking
     */
    public ArrayList<TrackedPeer> Find(String fname) throws RemoteException {
        HashSet<Integer> ids = fileMap.get(fname);
        if (ids == null) {  // no attached peer has it
            System.out.println("[SERVER]: not tracking requested file");
            return null;
        }
        ArrayList<TrackedPeer> answer = new ArrayList<TrackedPeer>();
        for (Integer nodeID : ids) {
            answer.add(peerInfo.get(nodeID.intValue()));
        }
        return answer;
    }

    /**
     * peer calls this on coordinator to update its respective file info
     * assumes that node calling has joined
     * @param fnames ArrayList of all file names that are shareable from peer
     * @param machID unique ID of peer, assuming no byzantine failures
     */
    public boolean UpdateList(ArrayList<String> fnames, int machID) throws RemoteException {
        if (peerInfo.get(machID) == null){
            System.out.println("[SERVER]: Peer is currently not part of the server.");
            return false;
        }

        peerInfo.get(machID).SetFiles(fnames);
        for (String fname : fnames) {
            if (!fileMap.containsKey(fname)) {  // add filename as key
                HashSet<Integer> newSet = new HashSet<Integer>();
                newSet.add(Integer.valueOf(machID));
                fileMap.put(fname, newSet);
            }
            else {  // key into structure, add machID if not already present
                fileMap.get(fname).add(Integer.valueOf(machID));  // vals of map are a set
            }
        }
        System.out.printf("[SERVER]: Updated with files from client with mach ID = %d.\n", machID);
        return true;
    }

    private void removeNode(int machID) {
        TrackedPeer peerToBeRemoved = peerInfo.get(machID);

        // for all file names associated with machID
        for (String fname : peerToBeRemoved.GetFiles()) {
            // remove machID from fileMap
            if (fileMap.containsKey(fname)) {
                // fileMap should always contain it, but check
                System.out.println("HERE!!");
                fileMap.get(fname).remove(Integer.valueOf(machID));
                // machID should exist in that set, but okay if not
                System.out.println(fname);
            }

            // if machID was only node associated with the file, remove it
            if (fileMap.get(fname).isEmpty()) {
                fileMap.remove(fname);
            }
        }

        // Set peerInfo for this particular ID back to null in case the peer node decides to join back at a later time.
        peerInfo.set(machID, null);
    }

    public static void main(String[] args){
        try{
            TrackerInterface server = new Tracker();
            Registry registry = LocateRegistry.createRegistry(serverPort);
            registry.rebind("TrackingServer", server);
            System.out.printf("\n[SERVER]: Tracking Server is ready at port %d. \n", serverPort);

            Timer timer = new Timer();
        } catch (Exception e){
            System.out.println("\n[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
    
}