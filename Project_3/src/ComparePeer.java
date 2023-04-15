import java.util.*;


public class ComparePeer implements Comparator<TrackedPeer> {

    // ascending order of Ping
    public int compare(TrackedPeer l, TrackedPeer r) {
        return l.GetPing() - r.GetPing();
    }

}