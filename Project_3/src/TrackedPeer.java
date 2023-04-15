import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NotBoundException;
import java.util.*;

/**
 * tracks peer info for nodes that server believes are up
 */
public class TrackedPeer {
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

    public PeerNodeInterface SetAndGetReference()
            throws NotBoundException, RemoteException {
        if (reference != null) {
            return reference;
        }
        Registry registry = LocateRegistry.getRegistry(addr, port);
        reference = (PeerNodeInterface) registry.lookup("mach" + id);
        return reference;
    }

    /**
     * override files with argument array
     */
    public void SetFiles(ArrayList<String> fnames) {
        this.files = fnames;
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
