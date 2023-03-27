import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ReadYourWritesStrategy implements ConsistencyStrategy {
    /**
     * Function for posting articles or replying to existing articles
     * @param article: Article to be published as a new post or as a reply
     * @param replyTo: Article ID we want to reply to. If it is 0, then we create a new post
     * @param selfServer: Server object that initially received the read request from the client.
     */
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        try{
            // Contact coordinator
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_" + selfServer.GetCoordNum());

            // Check if selfServer is the coordinator
            // This is for the case when the client sends its write request to the coordinator server itself
            int nextID = coord.GetCurrID();
            ArrayList<BulletinBoardServerInterface> serverList = coord.GetServerList();
            if (selfServer.GetServerPort() == selfServer.GetCoordPort()){
                if (!coord.UpdateTree(nextID, article, replyTo)) {
                    return false;
                }
                for (BulletinBoardServerInterface replica : serverList){
                    registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());
                    ServerToServerInterface peer = (ServerToServerInterface)
                        registry.lookup("BulletinBoardServer_" + replica.GetServerNumber());
                    if (!peer.UpdateTree(nextID, article, replyTo)) {
                        return false;
                    }
                }
            } else {
                // Get primary copy of article from coordinator
                selfServer.SetTree(coord.GetTree());
                if (!selfServer.UpdateTree(nextID, article, replyTo)) {
                    System.out.println("[SERVER]: Update operation was unsuccessful!");
                    return false;
                }

                // Send updated tree to every other server including the coordinator
                coord.SetTree(selfServer.GetTree());
                for (BulletinBoardServerInterface replica : serverList){
                    // Don't update the server that called this function since
                    // it was already updated above.
                    if (replica.GetServerNumber() == selfServer.GetServerNumber()) continue;

                    // Update all other replicas.
                    registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());
                    ServerToServerInterface peer = (ServerToServerInterface)
                        registry.lookup("BulletinBoardServer_" + replica.GetServerNumber());
                    if (!peer.UpdateTree(nextID, article, replyTo)) {
                        System.out.println("[SERVER]: Its possible some servers are currently offline. Please relaunch them!");
                        return false;
                    }
                }
            }
            
            coord.IncrementID();
            System.out.println("[SERVER]: Write operation was successful!");
            return true;
        } catch (Exception e){
            System.out.println("[SERVER]: Unable to post article/reply. Please restart the server!");
            return false;
        }  
    }

    /**
     * Function for updating all read quorum replicas using the overlapped server and returning a list
     * of bulletin board articles to the client.
     * @param selfServer: Function that initally received the read request from the client
     */
    public String ServerRead(BulletinBoardServer selfServer) {
        // Retrieve all articles defined in the content tree of the server
        String articles = selfServer.GetTree().ReadTree();
        if (articles.length() == 0){
            System.out.println("[SERVER]: No articles posted yet on the server.");
            return "";
        }
        System.out.println("[SERVER]: Read operation was successful!");
        return articles;
    }

    /**
     * Function which checks if all the read quorum servers agree on the latest value of the article ID being requested
     * and returns the article title and contents to the client.
     * @param selfServer: Server object that initally received the choose request from the client
     * @param articleID: Article ID being requested by the server
     */
    public String ServerChoose(BulletinBoardServer selfServer, int articleID) {
        // Retrieve article corresponding to the given ID
        ReferencedTree contentTree = selfServer.GetTree();
        String result = contentTree.GetAtIndex(articleID);
        if (result == null) {
            System.out.println("[SERVER]: Article not found for ID: " + articleID);
            return "";
        }
        return result;
    }

}