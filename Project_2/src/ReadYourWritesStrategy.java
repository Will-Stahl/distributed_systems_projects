import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ReadYourWritesStrategy implements ConsistencyStrategy {
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
                    try {
                        registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());
                        ServerToServerInterface peer = (ServerToServerInterface)
                            registry.lookup("BulletinBoardServer_" + replica.GetServerNumber());
                        if (!peer.UpdateTree(nextID, article, replyTo)) {
                            return false;
                        }
                    } catch (Exception e) {
                        System.out.println("[SERVER]: BulletinBoardServer_" + replica.GetServerNumber() +" is offline");
                        return false;
                    }
                }
            } else {
                // Get primary copy of article from coordinator
                selfServer.SetTree(coord.GetTree());
                System.out.println(selfServer.GetTree());
                
                if (!selfServer.UpdateTree(nextID, article, replyTo)) {
                    System.out.println("HERE!!!");
                    return false;
                }

                // Send updated tree to every other server including the coordinator
                coord.SetTree(selfServer.GetTree());

                System.out.println(serverList.size());
                for (BulletinBoardServerInterface replica : serverList){
                    // Don't update the server that called this function since
                    // it was already updated above.
                    if (replica.GetServerNumber() == selfServer.GetServerNumber()) continue;

                    // Update all other replicas.
                    registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());
                    ServerToServerInterface peer = (ServerToServerInterface)
                        registry.lookup("BulletinBoardServer_" + replica.GetServerNumber());
                    if (!peer.UpdateTree(nextID, article, replyTo)) {
                        return false;
                    }
                }
            }
            
            coord.IncrementID();
            return true;
        } catch (Exception e){
            System.out.println("[SERVER]: Unable to post article. Please restart the server!");
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
        String result = contentTree.GetAtIndex(articleID);
        if (result == null) {
            return "[SERVER]: Article not found for ID: " + articleID;
        }
        return result;
    }

}