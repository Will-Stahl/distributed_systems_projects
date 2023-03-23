import java.rmi.RemoteException;
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

            Sync(nextID, article, replyTo, coord);

            // Generate new ID for future articles
            coord.IncrementID();
            return true;
        } catch (Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
            System.out.println("[SERVER]: Make sure coordinator is online!");
        }
    }

    public String ServerRead(BulletinBoardServer selfServer) {
        try {
            // Contact coordinator to initiate quorum
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_5");

            List<BulletinBoardServerInterface> readQuorum = coord.GetReadQuorum();
            List<BulletinBoardServerInterface> writeQuorum = coord.GetWriteQuorum();

            // Cant establish a read quorum if a write quorum already doesn't exist
            if (writeQuorum.size() == 0){
                System.out.println("HERE!!!");
                return "";
            }

            int numSuccessfulReads = 0;
            Set<String> responses = new HashSet<>();
            String response = "";
            for (BulletinBoardServerInterface replica : readQuorum){
                try {
                    response = replica.GetTree().ReadTree();
                    responses.add(response);
                    System.out.println("RESPONSE: " + response);
                    numSuccessfulReads += 1;
                } catch (Exception e) {
                    System.out.println("[SERVER]: Server is offline. Please restart it.");
                }
            }
            System.out.println("Read size: " + readQuorum.size() + " NR: " + NR + " Succes: " + numSuccessfulReads);
            if (numSuccessfulReads == NR && responses.size() == 1) {
                System.out.println("[SERVER]: Read Quorum agreed on the same read value.");
                return response;
            }

            System.out.println("[SERVER]: Read Quorum did not agree on the same read value.");
        } catch (Exception e) {
            System.out.println("[SERVER]: Make sure coordinator is online!");
        }
        return "";
    }

    public String ServerChoose(BulletinBoardServer selfServer, int articleID, ReferencedTree contentTree) {
        /* 
        try {
            
            // Contact coordinator to initiate quorum
            Registry registry = LocateRegistry.getRegistry(selfServer.GetCoordHost(), selfServer.GetCoordPort());
            BulletinBoardServerInterface coord = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_5");

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
            e.printStackTrace();
            System.out.println("[SERVER]: Make sure coordinator is online!");
        }
        Random rand = new Random();*/
        return "";
        //return readQuorum.get(rand.nextInt(readQuorum.size())).GetTree().GetAtIndex(articleID);
    }

}