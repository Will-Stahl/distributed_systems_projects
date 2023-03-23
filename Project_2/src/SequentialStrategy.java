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
            boolean result = true;
            if (!selfServer.GetTree().AddNode(nextID, article, replyTo)) {
                System.out.println("[SERVER]: Unable to post article/reply to bulletin board.");
                return false;  // local update failed, do not update others
            }
    
            ArrayList<BulletinBoardServerInterface> serverList = selfServer.GetServerList();
            for (BulletinBoardServerInterface replica : serverList){
                registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());
                ServerToServerInterface peer = (ServerToServerInterface)
                    registry.lookup("BulletinBoardServer_" + replica.GetServerNumber());
                if (!peer.UpdateTree(nextID, article, replyTo)) {
                    result = false;
                }
            }
            
            selfServer.IncrementID();
            return result;
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
        return selfServer.GetTree().ReadTree();
    }

    /**
     * @param articleID article requested by client
     * @param contentTree article tree from server object that called this
     * sequential consistency, just read from local
     */
    public String ServerChoose(BulletinBoardServer selfServer, int articleID, ReferencedTree contentTree) {
        HashMap<Integer, String> articleMap = contentTree.ParseTree(contentTree.ReadTree());

        // If invalid key, then return error message.
        if (!articleMap.containsKey(articleID)){
            return "[CLIENT]: Article not found for ID: " + articleID;
        }

        return articleMap.get(articleID);
    }

}