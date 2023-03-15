import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;


// TODO: use blocking or nonblocking?
// Nonblocking probably can't guarantee this consistency
public class ReadYourWritesStrategy implements ConsistencyStrategy {
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        // TODO: Check if at least 2 servers are online
    
        // Only coordinator server can make updates locally
        try{
            // Contact coordinator
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());

            // Check if the object calling ServerPublish is itself the coordinator
            BulletinBoardServer server = selfServer;
            if (selfServer.GetServerPort() != selfServer.GetCoordPort()){
                server = (BulletinBoardServer) registry.lookup("BulletinBoardServer_" + selfServer.GetCoordNum());
            }

            // Update data only on the central server
            int nextID = selfServer.GetCurrID();
            if (!selfServer.GetTree().AddNode(nextID, article, replyTo)) {
                return false;  // local update failed, do not update others
            }
            
            // TODO: Once primary server has made its updates, lazily update the read servers (will need a thread here to do that)
            return true;
        } catch (Exception e){
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
    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        String result = contentTree.GetAtIndex(articleID);
        if (result == null) {
            return "Article not found for ID: " + articleID;
        }
        return result;
    }

}