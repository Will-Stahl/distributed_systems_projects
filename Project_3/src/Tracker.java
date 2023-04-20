import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Tracker extends UnicastRemoteObject implements TrackerInterface {
    // port# like 8000 is too likely to be used by someone else on lab machine
    private static int serverPort = 11396;

    private static Registry registry;

    // data structure to store joined peers
    private static ArrayList<TrackedPeer> peerInfo;
    // known files and their locations
    private static HashMap<String, FileInfo> fileMap;

    public Tracker() throws RemoteException {
        fileMap = new HashMap<String, FileInfo>();
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
            System.out.printf("[SERVER]: Added peer node with ID %d at IP: %s and Port: %d.\n", machID, IP, Port);
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
    public FileInfo Find(String fname) throws RemoteException {
        if (fileMap.get(fname) == null
                || fileMap.get(fname).getMembers() == null) {
            System.out.println("[SERVER]: Not currently tracking requested file");
            return null;
        }

        return fileMap.get(fname);
    }

    /**
     * peer calls this on coordinator to update its respective file info
     * assumes that node calling has joined
     * @param fnames ArrayList of all file names that are shareable from peer
     * @param machID unique ID of peer, assuming no byzantine failures
     */
    public boolean UpdateList(ArrayList<FileInfo> fData, int machID) throws RemoteException {
        if (peerInfo.get(machID) == null){
            System.out.println("[SERVER]: Peer is currently not part of the server.");
            return false;
        }

        ArrayList<String> fnames = new ArrayList<String>();
        for (FileInfo finfo : fData) {
            fnames.add(finfo.getName());
        }  // refactored to send names with checksum
        peerInfo.get(machID).SetFiles(fnames);

        for (FileInfo finfo : fData) {
            if (!fileMap.containsKey(finfo.getName())) {  // add filename as key
                finfo.add(peerInfo.get(machID));
                fileMap.put(finfo.getName(), finfo);
            }
            else {  // key into structure, add machID if not already present
                // vals of map are a set
                fileMap.get(finfo.getName()).add(peerInfo.get(machID));
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
                fileMap.get(fname).remove(peerInfo.get(machID));
                // machID should exist in that set, but okay if not
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
            registry = LocateRegistry.createRegistry(serverPort);
            for (int i = 0; i < 5; i++) {
                try {
                    registry.unbind("Peer_" + i);
                } catch (Exception e) {}
            }
            registry.rebind("TrackingServer", server);
            System.out.printf("\n[SERVER]: Tracking Server is ready at port %d. \n", serverPort);

            // Periodically ping every peer node that is currently online
            Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    public void run(){
                            for (int i = 0; i < peerInfo.size(); i++){
                                try {
                                    if (peerInfo.get(i) != null){
                                        PeerNodeInterface peer = (PeerNodeInterface) registry.lookup("Peer_" + i);
                                        peer.Ping();
                                        // TODO: Uncomment once done with debugging.
                                        //System.out.printf("[SERVER]: Peer with MachID = %d is online!\n", i);
                                    }
                                } catch (Exception e){
                                    // If the peer is offline, then set the object value to null
                                    peerInfo.set(i, null);
                                }
                            }
                    }
                };
            timer.schedule(task, 0, 2000);

        } catch (Exception e){
            System.out.println("\n[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
    
}