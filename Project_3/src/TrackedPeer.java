
/**
 * tracks peer info for nodes that server believes are up
 */
public class TrackedPeer {
    private int id;
    private int port;
    private String addr;
    private ArrayList<String> files;

    public TrackedPeer(int id, int port, String addr) {
        this.id = id;
        this.port = port;
        this.addr = addr;
        this.files = new ArrayList<String>();
    }

    public TrackedPeer(int id, int port, String addr, ArrayList<String> files) {
        this.id = id;
        this.port = port;
        this.addr = addr;
        this.files = files;
    }

    public TrackedPeer GetCopy() {
        return new TrackedPeer(id, port, addr, files.clone());
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
}