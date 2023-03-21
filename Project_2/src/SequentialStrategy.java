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
            return false;  // local update failed, do not update others
        }

        ArrayList<BulletinBoardServerInterface> serverList = selfServer.GetServerList();
        for (BulletinBoardServerInterface replica : serverList){
            try {
                //System.out.println(selfServer.GetServerHost() + " " + selfServer.GetServerPort());
                registry =  LocateRegistry.getRegistry(replica.GetServerHost(), replica.GetServerPort());

                // Fixed set of ports are mapped to specific server numbers
                HashMap<Integer, Integer> portToServerMap = new HashMap<>();
                portToServerMap.put(2000, 1);
                portToServerMap.put(2001, 2);
                portToServerMap.put(2002, 3);
                portToServerMap.put(2003, 4);

                ServerToServerInterface peer = (ServerToServerInterface)
                    registry.lookup("BulletinBoardServer_" + portToServerMap.get(replica.GetServerPort()));
                if (!peer.UpdateTree(nextID, article, replyTo)) {
                    result = false;
                }
            } catch (Exception e) {
                System.out.println("[SERVER]: BulletinBoardServer_ is offline");
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
            return "[SERVER]: Article not found for ID: " + articleID;
        }
        return result;
    }

}