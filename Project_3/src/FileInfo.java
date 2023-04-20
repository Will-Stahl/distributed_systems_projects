import java.util.*;
import java.io.Serializable;

/**
 * class tracks file name with its respective checksum
 * and if used by the tracker, tracks who has the file
 * pseudo-interface of ArrayList so that we can use get(i) for peers
 */
public class FileInfo implements Serializable {

    private String fileName;  // needed on peer side
    private long checksum;  // useable in CRC32 class
    private ArrayList<TrackedPeer> possessors;

    public FileInfo(long checksum) {
        this.checksum = checksum;
        possessors = new ArrayList<TrackedPeer>();
    }

    public FileInfo(String fileName, long checksum) {
        this.fileName = fileName;
        this.checksum = checksum;
        possessors = new ArrayList<TrackedPeer>();
    }

    // probably unused
    public FileInfo() {
        possessors = new ArrayList<TrackedPeer>();
    }

    public long getChecksum() {
        return checksum;
    }

    public String getName() {
        return fileName;
    }

    public TrackedPeer get(int i) {
        return possessors.get(i);
    }

    public void set(int i, TrackedPeer val) {
        possessors.set(i, val);
    }

    public void add(TrackedPeer val) {
        possessors.add(val);
    }

    public void remove(TrackedPeer toRemove) {
        possessors.remove(toRemove);
    }

    public ArrayList<TrackedPeer> getMembers() {
        return possessors;
    }

    public boolean isEmpty() {
        return possessors.isEmpty();
    }

    public boolean contains(TrackedPeer peer) {
        return possessors.contains(peer);
    }

}