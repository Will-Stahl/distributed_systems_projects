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
        try{
            // Contact coordinator
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_" + selfServer.GetCoordNum());

            // Get primary copy of article from coordinator
            String primaryCopy = coord.GetTree().GetAtIndex(replyTo);

            // Update copy on the server that called this function.
            registry =  LocateRegistry.getRegistry(selfServer.GetServerHost(), selfServer.GetServerPort());
            System.out.println(selfServer.GetServerNumber());
            ServerToServerInterface peer = (ServerToServerInterface)
                    registry.lookup("BulletinBoardServer_" + selfServer.GetServerNumber());

            int nextID = selfServer.GetCurrID();
            
            if (!peer.UpdateTree(nextID, article, replyTo)) {
                return false;
            }
            selfServer.IncrementID();
            
            System.out.println(coord.GetServerList().size());
            
            // Update all other replicas and coordinate server (if necessary)
            return true;
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("[SERVER]: ERROR!");
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
            return "[SERVER]: Article not found for ID: " + articleID;
        }
        return result;
    }

}