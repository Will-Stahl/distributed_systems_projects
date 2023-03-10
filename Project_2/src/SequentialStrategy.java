import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class SequentialStrategy implements ConsistencyStrategy {
    public boolean ServerPublish(String article, Integer selfServerNum,
                                    Integer nextID) {
        // TODO: tell all other servers to update, update self
        // backups acknowledge via return val
        Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
        for (Integer i = 2000; i < 2005; i++) {
            if (i - 1999 == selfServerNum) {  // do not contact self
                continue;
            }
            ServerToServerInterface peer = (ServerToServerInterface)
                registry.lookup("BulletinBoardServer_" + i - 1999);
            peer.UpdateTree(/* TODO: parameters are undecided*/);  // specify what this message replies to
        }
        return false;
    }

    public String ServerRead() {
        return "";
    }

    public String ServerChoose(Integer articleID) {
        return "";
    }

    public boolean ServerReply(String article) {
        return false;
    }
}