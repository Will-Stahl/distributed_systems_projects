import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * tracks peer info for nodes that server believes are up
 */
public class TrackedPeer implements Serializable {
    private int id;
    private int port;
    private String addr;
    private ArrayList<String> files;
    private PeerNodeInterface reference;
    private int ping;

    public TrackedPeer(int id, int port, String addr) {
        this.id = id;
        this.port = port;
        this.addr = addr;
        this.files = new ArrayList<String>();
        reference = null;
        ping = 0;
    }

    public TrackedPeer(int id, int port, String addr, ArrayList<String> files) {
        this.id = id;
        this.port = port;
        this.addr = addr;
        this.files = files;

        reference = null;
        ping = 0;
    }

    // public TrackedPeer GetCopy() {
    //     return new TrackedPeer(id, port, addr, files.clone());
    // }

    /**
     * sets reference if not set and returns it
     */
    public PeerNodeInterface SetAndGetReference(String serverHostname, int serverPort)
            throws NotBoundException, RemoteException {
        if (reference != null) {
            return reference;
        }
        Registry registry = LocateRegistry.getRegistry(serverHostname, serverPort);
        reference = (PeerNodeInterface) registry.lookup("Peer_" + id);
        return reference;
    }

    // public PeerNodeInterface GetReference() {
    //     return reference;
    // }

    // public void SetReference(PeerNodeInterface reference) {
    //     this.reference = reference;
    // }

    /**
     * override files with argument array
     */
    public void SetFiles(ArrayList<String> fnames) {
        this.files = fnames;

        // tracker calls this method, so it shouldn't do the following
        // Compute and store checksums for each file.
        // checkSumMap = new HashMap<>();
        // for (int i = 0; i < files.size(); i++){
        //     String path = "files/mach" + id + "/" + files.get(i);
        //     File file = new File(path);
        //     try {
        //         FileInputStream fis = new FileInputStream(file);
        //         byte[] byteArray = new byte[(int) file.length()];
        //         fis.read(byteArray);
        //         fis.close();

        //         FileDownload info = new FileDownload(byteArray);
        //         checkSumMap.put(files.get(i), info.GetChecksum());
        //     } catch (Exception e){
        //         System.out.println("[SERVER]: Error occured while reading file. Please ensure the file is present in the directory.");
        //     }
        // }
    }

    public int GetID() {
        return id;
    }

    public int GetPort() {
        return port;
    }

    public String GetAddr() {
        return addr;
    }

    public ArrayList<String> GetFiles() {
        return files;
    }

    public int GetPing() {
        return ping;
    }

    public void SetPing(int ping) {
        this.ping = ping;
    }

}
