import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class SequentialStrategy implements ConsistencyStrategy {

    /**
     * @param nextID article ID to reply to
     * @param article contents of article to publish
     * @param replyTo article ID to reply to
     * @param selfServer server object that called this function
     * returns false if underlying tree object returns false
     */
    public boolean ServerPublish(int nextID, String article, int replyTo,
                        BulletinBoardServer selfServer) {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(
                    selfServer.GetCoordHost(), selfServer.GetCoordPort());
        } catch (RemoteException e) {
            return false;
        }
        boolean result = true;

        // update self
        if (selfServer.GetTree().AddNode(nextID, article, replyTo)) {
            return false;  // local update failed, do not update others
        }

        // RMI others to update
        for (int i = 1; i <= 5; i++) {
            if (i == selfServer.GetServerNumber()) {  // do not contact self
                continue;
            }
            try {
                ServerToServerInterface peer = (ServerToServerInterface)
                    registry.lookup("BulletinBoardServer_" + i);
                if (!peer.UpdateTree(nextID, article, replyTo)) {
                    result = false;
                }
            } catch (Exception e) {
                result = false;
            }
        }
        return result;
    }

    public String ServerRead() {
        return "";
    }

    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        String result = contentTree.GetAtIndex(articleID);
        if (result == null) {
            return "Article not found for ID: " + articleID;
        }
        return result;
    }

    public boolean ServerReply(String article) {
        return false;
    }
}