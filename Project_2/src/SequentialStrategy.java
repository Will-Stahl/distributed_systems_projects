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
     * @param selfServerNum prevents from self-messaging
     * @param contentTree pass in tree object so self can update it
     * returns false if underlying tree object returns false
     */
    public boolean ServerPublish(int nextID, String article, int replyTo,
                        int selfServerNum, ReferencedTree contentTree) {
        Registry registry = LocateRegistry.getRegistry(coordHost, coordPort);
        result = true;

        // update self
        if (contentTree.AddNode(nextID, article, replyTo)) {
            return false;  // local update failed, do not update others
        }

        // RMI others to update
        for (int i = 1; i <= 5; i++) {
            if (i == selfServerNum) {  // do not contact self
                continue;
            }
            ServerToServerInterface peer = (ServerToServerInterface)
                registry.lookup("BulletinBoardServer_" + i);
            if (!peer.UpdateTree(nextID, article, replyTo)) {
                result = false;
            }
        }
        return result;
    }

    public String ServerRead() {
        return "";
    }

    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        String result;
        if (result = contentTree.GetAtIndex(articleID) == null) {
            return "Article not found for ID: " + articleID;
        }
        return result;
    }

    public boolean ServerReply(String article) {
        return false;
    }
}