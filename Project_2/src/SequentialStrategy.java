import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class SequentialStrategy implements ConsistencyStrategy {

    /**
     * @param article contents of article to publish
     * @param replyTo article ID to reply to (0 if not a reply)
     * @param selfServer server object that called this function
     * returns false if underlying tree object returns false
     */
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(
                    selfServer.GetCoordHost(), selfServer.GetCoordPort());
        } catch (RemoteException e) {
            return false;
        }

        // not the central server
        if (selfServer.GetServerPort() != selfServer.GetCoordPort()) {
            try {
                ServerToServerInterface coord = (ServerToServerInterface)
                            registry.lookup(
                            "BulletinBoardServer_" + selfServer.GetCoordNum());
                return coord.CoordinatorPost(article, replyTo);
            } catch (Exception e) {
                return false;
            }
        }

        // is the central server, update self
        int nextID = selfServer.GetCurrID();
        boolean result = true;
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
        selfServer.IncrementID();
        return result;
    }

    /**
     * @param selfServer server object that called this method
     * sequential consistency, just read from local
     */
    public String ServerRead(BulletinBoardServer selfServer) {
        return selfServer.GetTree().ReadTree();
    }

    /**
     * @param articleID article requested by client
     * @param contentTree article tree from server object that called this
     * sequential consistency, just read from local
     */
    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        String result = contentTree.GetAtIndex(articleID);
        if (result == null) {
            return "Article not found for ID: " + articleID;
        }
        return result;
    }

}