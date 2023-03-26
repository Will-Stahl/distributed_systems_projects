import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class QuorumStrategy implements ConsistencyStrategy {
    private static int NR = 3;
    private static int NW = 3;

    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        try{    
            // Contact coordinator to initiate quorum
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_" + selfServer.GetCoordNum());

            // Initialize random servers to read and write quorums
            coord.SetQuorums(NR, NW);

            // Get current server list
            ArrayList<BulletinBoardServerInterface> serverList = coord.GetServerList();
            if (serverList.size() != NR + NW - 1) {
                System.out.println("[SERVER]: Please ensure all 5 servers are running!");
                return false;
            }

            // Update all writeQuorum servers
            List<BulletinBoardServerInterface> writeQuorum = coord.GetWriteQuorum();
            int numberOfSuccessfulWrites = 0;
            int nextID = coord.GetCurrID();
            for (BulletinBoardServerInterface writeServer : writeQuorum){
                registry =  LocateRegistry.getRegistry(writeServer.GetServerHost(), writeServer.GetServerPort());
                ServerToServerInterface peer = (ServerToServerInterface)
                    registry.lookup("BulletinBoardServer_" + writeServer.GetServerNumber());
                if (peer.UpdateTree(nextID, article, replyTo)) {
                    numberOfSuccessfulWrites += 1;
                }
            }

            if (numberOfSuccessfulWrites != NW) {
                System.out.println("[SERVER]: Article not posted! Majority of servers did not agree on the write.");
                return false;
            }
            
            System.out.println("[SERVER]: Write quorum has agreed to post the article.");

            // Update other servers
            Sync(nextID, article, replyTo, coord);

            // Generate new ID for future articles
            coord.IncrementID();
            return true;
        } catch (Exception e){
            System.out.println("[SERVER]: One of the servers in the write quorum is offline. Please restart it.");
            return false;
        }
    }

    // Lazily update all read quorum servers
    public void Sync(int nextID, String article, int replyTo, BulletinBoardServerInterface coord){
        try {
            List<BulletinBoardServerInterface> readQuorum = coord.GetReadQuorum();
            BulletinBoardServerInterface overlappedServer = readQuorum.get(0);

            for (BulletinBoardServerInterface replica : readQuorum){
                try {
                    // Don't update the overlapped server since it already contains the latest updates.
                    if (replica.GetServerNumber() == overlappedServer.GetServerNumber()){
                        continue;
                    } else {
                        // Update replica with the latest writes from the write quorum
                        replica.SetTree(overlappedServer.GetTree());
                    }
                } catch (Exception e) {
                    // The server can be updated at a later time as this function is called periodically in the background.
                    System.out.println("[SERVER]: Error occurred while updating server. Please restart the server!");
                    continue;
                }
            }
        } catch (Exception e){
            System.out.println("[SERVER]: Make sure coordinator is online!");
        }
    }

    public String ServerRead(BulletinBoardServer selfServer) {
        try {
            if (selfServer.GetTree().ReadTree().length() == 0){
                System.out.println("[SERVER]: No articles posted yet on the server.");
                return "";
            }
            
            // Contact coordinator to initiate quorum
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_5");

            List<BulletinBoardServerInterface> readQuorum = coord.GetReadQuorum();

            int numSuccessfulReads = 0;
            Set<String> responses = new HashSet<>();
            String response = "";
            for (BulletinBoardServerInterface replica : readQuorum){
                try {
                    response = replica.GetTree().ReadTree();
                    responses.add(response);
                    numSuccessfulReads += 1;
                } catch (Exception e) {
                    System.out.println("[SERVER]: Server is offline. Please restart it.");
                }
            }
            
            // Make sure all read quorum servers agree on the read value
            // and that the read value is the same across all of them, 
            // hence the "responses.size() == 1" check where responses
            // is a hashset that contains only unique response values.
            if (numSuccessfulReads == NR && responses.size() == 1) {
                System.out.println("[SERVER]: Read Quorum agreed on the same read value.");
                return response;
            }

            System.out.println("[SERVER]: Read Quorum did not agree on the same read value.");
            System.out.println("[SERVER]: It's possible that one of the read servers has gone offline.");
        } catch (Exception e) {
            System.out.println("[SERVER]: Make sure coordinator is online!");
        }
        return "";
    }

    public String ServerChoose(BulletinBoardServer selfServer, int articleID, ReferencedTree contentTree) {
        try {
            if (selfServer.GetTree().ReadTree().length() == 0){
                System.out.println("[SERVER]: No articles posted yet on the server.");
                return "[SERVER]: Article not found for ID: " + articleID;
            }

            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_5");

            List<BulletinBoardServerInterface> readQuorum = coord.GetReadQuorum();

            int numSuccessfulReads = 0;
            Set<String> responses = new HashSet<>();
            String response = "";

            for (BulletinBoardServerInterface replica : readQuorum){
                try {
                    String readResult = replica.GetTree().GetAtIndex(articleID);
                    if (readResult == null){
                        System.out.println("[SERVER]: Article ID does not exist.");
                        return "[SERVER]: Article not found for ID: " + articleID;
                    }
                    responses.add(readResult);
                    numSuccessfulReads += 1;
                    response = readResult;
                } catch (Exception e) {
                    // The server can be updated at a later time as this function is called periodically in the background.
                    System.out.println("[SERVER]: Error occurred while updating server. Please restart the server!");
                    continue;
                }
            }

            if (numSuccessfulReads == NR && responses.size() == 1) {
                System.out.println("[SERVER]: Read Quorum agreed on the same read value.");
                return response;
            }

        } catch (Exception e){
            System.out.println("[SERVER]: Make sure coordinator is online!");
        }
        return "[SERVER]: Article not found for ID: " + articleID;
    }

}