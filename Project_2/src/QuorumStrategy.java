import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class QuorumStrategy implements ConsistencyStrategy {
    // this strategy will need some extra member variables (NR, NW)
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        return false;
    }

    public String ServerRead(BulletinBoardServer selfServer) {
        return "";
    }

    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        return "";
    }

}