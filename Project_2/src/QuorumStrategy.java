import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.util.*;

public class QuorumStrategy implements ConsistencyStrategy {
    private static int NR = 3;
    private static int NW = 3;
    private static ArrayList<BulletinBoardServer> readQuorum = new ArrayList<>();
    private static ArrayList<BulletinBoardServer> writeQuorum = new ArrayList<>();

    // Keep track of server that is both in the read and write quorum
    private static ServerToServerInterface overlappedServer;

    // TODO: Complete this function
    private static void CreateRandomReadWriteQuorums(){

    }

    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        try{
            // Contact coordinator
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());

            // Check if the object calling ServerPublish is itself the coordinator
            BulletinBoardServer server = selfServer;
            if (selfServer.GetServerPort() != selfServer.GetCoordPort()){
                server = (BulletinBoardServer) registry.lookup("BulletinBoardServer_" + selfServer.GetCoordNum());
            }
            // Initialize write quorum if it has never been initialized before
            if (writeQuorum.size() == 0){
                CreateRandomReadWriteQuorums();
            }

            // Perform write operation to writeQuorum servers and track successful writes
            ArrayList<ServerToServerInterface> updatedServers = new ArrayList<>();

            // Get article ID
            int nextID = server.GetCurrID();

            // RMI write quorum servers to update
            for (int i = 1; i <= writeQuorum.size(); i++){
                try {
                    ServerToServerInterface peer = writeQuorum.get(i);
                    if (!peer.UpdateTree(nextID, article, replyTo)) {
                        // Unable to update tree, so move onto next server
                        continue;
                    }
                    updatedServers.add(peer);
                } catch (Exception e) {
                    // Unable to update tree, so move onto next server
                    continue;
                }
            }

            // Check if any server writes were unsuccesful. 
            // If server write was unsuccesful, then undo changes to the updated servers
            if (updatedServers.size() != writeQuorum.size()){
                System.out.println("[SERVER]: Write operation was unsuccesful. Undoing changes to updated servers...");
                // TODO: Undo write changes to servers that were updated
                return false;
            }

            // Generate new ID for future articles
            selfServer.IncrementID();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    // Lazily update all read quorum servers
    // TODO: Write code for calling this function periodically
    public boolean UpdateAllReadServers(int nextID, String article, int replyTo){
        // TODO: This needs more updates to use the overlappedServers variable
        try{
            for (int i = 0; i < readQuorum.size(); i++){
                try {
                    ServerToServerInterface peer = readQuorum.get(i);
                    if (!peer.UpdateTree(nextID, article, replyTo)) {
                        // Unable to update tree, so move onto next server
                        continue;
                    }
                } catch (Exception e) {
                    // Unable to update tree, so move onto next server
                    continue;
                }
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    // TODO: Probably needs refactoring
    public String ServerRead(BulletinBoardServer selfServer) {
        // If read and write quorums have not been created earlier, then create them right now
        if (readQuorum.size() == 0){
            CreateRandomReadWriteQuorums();
        }

        ArrayList<String> readResults = new ArrayList<>();
        try{
            for (int i = 0; i < readQuorum.size(); i++){
                try {
                    BulletinBoardServer server = readQuorum.get(i);
                    readResults.add(server.GetTree().ReadTree());
                } catch (Exception e) {
                    // Unable to update tree, so move onto next server
                    continue;
                }
            }
            if (readResults.size() == readQuorum.size()){
                System.out.println("[SERVER]: Read Operation was successful! Printing results now...");
                return String.join(",", readResults);
            }
            return "";
        } catch (Exception e){
            return "";
        }
    }

    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        Random rand = new Random();
        return readQuorum.get(rand.nextInt(readQuorum.size())).GetTree().GetAtIndex(articleID);
    }

}