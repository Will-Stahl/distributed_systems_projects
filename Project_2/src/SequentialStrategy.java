import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class SequentialStrategy implements ConsistencyStrategy {
    /**
     * @param article contents of article to publish
     * @param replyTo article ID to reply to (0 if not a reply)
     * @param selfServer server object that called this function
     * returns false if underlying tree object returns false
     */
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            } catch (RemoteException e) {
                System.out.println("[SERVER]: Make sure coordinator is online.");
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
                    System.out.println("[SERVER]: Central Server is offline. Please try again once it's online.");
                    return false;
                }
            }
    
            // is the central server, update self
            int nextID = selfServer.GetCurrID();
            if (!selfServer.GetTree().AddNode(nextID, article, replyTo)) {
                System.out.println("[SERVER]: Unable to post article/reply to bulletin board.");
                return false;  // local update failed, do not update others
            }
    
            // Update all the other replicas
            ArrayList<BulletinBoardServerInterface> serverList = selfServer.GetServerList();
            for (BulletinBoardServerInterface replica : serverList){
                registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());
                ServerToServerInterface peer = (ServerToServerInterface)
                    registry.lookup("BulletinBoardServer_" + replica.GetServerNumber());
                if (!peer.UpdateTree(nextID, article, replyTo)) {
                    System.out.println("[SERVER]: One or more servers have failed to update.");
                }
            }
            
            // Increment the article ID for a subsequent article
            selfServer.IncrementID();
            System.out.println("[SERVER]: Write operation was successful!");
            return true;
        } catch (Exception e){
            System.out.println("[SERVER]: Unable to post article/reply. Please restart the server!");
            return false;
        }
    }

    /**
     * @param selfServer server object that called this method
     * sequential consistency, just read from local
     */
    public String ServerRead(BulletinBoardServer selfServer) {
        String articles = selfServer.GetTree().ReadTree();
        if (articles.length() == 0){
            System.out.println("[SERVER]: No articles posted yet on the server.");
            return "";
        }
        System.out.println("[SERVER]: Read operation was successful!");
        return articles;
    }

    /**
     * @param articleID article requested by client
     * @param contentTree article tree from server object that called this
     * sequential consistency, just read from local
     */
    public String ServerChoose(BulletinBoardServer selfServer, int articleID, ReferencedTree contentTree) {
        String result = contentTree.GetAtIndex(articleID);
        if (result == null) {
            System.out.println("[SERVER]: Article not found for ID: " + articleID);
            return "";
        }
        return result;
    }

}