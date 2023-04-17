import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class PeerNode extends UnicastRemoteObject implements PeerNodeInterface {
    private static String dirPath;
    private static String IP;
    private static int port;
    private static int machID;
    private static int[][] latencies;
    private static ArrayList<String> fnames;  // TODO: make thread-safe
    private static TrackerInterface server;
    private static String serverHostname;
    private static AtomicInteger numTasks;  // thread safe measure of load

    public PeerNode() throws RemoteException {}
    
    public int GetLoad() throws RemoteException {
        return numTasks.get();
    }

    /**
     * call on peer that has already been selected, is RMI
     * calculates checksum and returns it
     */
    public FileDownload Download(String fname) throws RemoteException {
        numTasks.incrementAndGet();  // must cleanup before every return
        byte[] contents;
        try {
            contents = Files.readAllBytes(Paths.get(dirPath + fname));
        }
        catch (IOException|SecurityException|InvalidPathException e) {
            System.out.println("[PEER]: Peer requested non-present file: "
                + fname + " or permissions/IO error");
            numTasks.decrementAndGet();  // cleanup
            return null;
        }
        numTasks.decrementAndGet();
        // limitation: transmission takes time and occurs after load decremented
        return new FileDownload(contents);
    }

    public void Ping() throws RemoteException{}

    /**
     * peer side logic of determining which client to download from
     * tries best peer up to 2 times if checksum fails or RemoteException
     * moves on to next best peer if previous fails
     * @param fname name of file searching for
     */
    private static boolean DownloadAsClient(String fname) {
        numTasks.incrementAndGet();  // must cleanup before every return

        ArrayList<TrackedPeer> candidates = null;
        try {
            candidates = server.Find(fname);
        } catch (RemoteException e) {
            // careful to decrement thread-shared variable
            numTasks.decrementAndGet();
            return false;
        }
        ArrayList<PeerNodeInterface> refs = new ArrayList<PeerNodeInterface>();
        int[] loadOrder = new int[candidates.size()];
        for (TrackedPeer candidate : candidates) {
            try {
                PeerNodeInterface ref = candidate.SetAndGetReference();
                int ping = ref.GetLoad()/** TODO: times latency */;
                candidate.SetPing(ping);
            } catch (NotBoundException|RemoteException e) {
                candidate = null;
            }
        }
        while (candidates.remove(null));  // remove all nulls
        candidates.sort(new ComparePeer());

        for (TrackedPeer candidate : candidates) {

            // attempt to recover from various failure cases
            PeerNodeInterface ref = null;
            FileDownload dl = null;
            try {
                ref = candidate.SetAndGetReference();
                dl = ref.Download(fname);
            } catch (NotBoundException e) {
                continue;  // move on
            } catch (RemoteException f) {
                // try one more time
                try {
                    ref.Download(fname);
                } catch (RemoteException g) {
                    System.out.println("[PEER]: failed to download from peer "
                                            + candidate.GetID());
                    continue;  // move on from twice-failed peer
                }
            }

            if (dl == null) {
                continue;  // peer didn't have it
            }

            try {
                if (!dl.Checksum()) {  // if checksum fails
                    dl = ref.Download(fname);  // try same peer again
                }
                if (!dl.Checksum()) {  // if it breaks again
                    continue;  // move on from this peer
                }

                // it worked if you get here, save the file
                Files.write(Paths.get(dirPath + fname), dl.GetContents());
                numTasks.decrementAndGet();
                return true;
            } catch (RemoteException e) {
                continue;
            } catch (IOException f) {
                System.out.println("[PEER]: error when writing to file");
                numTasks.decrementAndGet();
                return false;
            }
        }

        numTasks.decrementAndGet();
        return false;
    }

    // Function for setting a random port number
    private static int GetRandomPortNumber(){
        Random rand = new Random();
        return (rand.nextInt((65535 - 1024) + 1)) + 1024;
    }

    //Function for displaying menu options
    private static void DisplayOptions(){
        System.out.println("\n------------------------ Available Commands ------------------------");
        System.out.println("1. Enter \"Join\" to join the tracking server");
        System.out.println("2. Enter \"Leave\" to leave the tracking server");
        System.out.println("3. Enter \"Find: <File Name>\" to find a file in the shared directory.");
        System.out.println("4. Enter \"Download: <File Name>\" to download a file from a peer.");
    }

    private static void SendClientRequestToServer(PeerNode peer){
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("\n[PEER]: Enter command: ");
            String request = sc.nextLine();
            
            if (request.equalsIgnoreCase("join") || request.equalsIgnoreCase("leave")){
                peer.HandleJoinAndLeave(request);
            } else if (request.contains(":")) {
                String[] parts = request.split(":");
                parts[0] = parts[0].trim().toLowerCase();

                // Handle "find" and "download" requests
                if ((parts[0].startsWith("find") && ValidateFindAndDownloadRequest(request)) || 
                    (parts[0].startsWith("download") && ValidateFindAndDownloadRequest(request))){
                    HandleFindAndDownloadRequest(request);
                } else {
                    DisplayOptions();
                }
            } else {
                System.out.println("[PEER]: Colon is missing. Use format \"Find: <File Name>\" or \"Download: <File Name>\"");
                DisplayOptions();
            }
        }
    }

    /**
     * Helper function for validating a find or download request from the peer.
     * @param request: Request string entered by the peer node in the command line.
     * @return: true or false value indicating a valid or invalid request respectively.
     */
    private static boolean ValidateFindAndDownloadRequest(String request){
        // If "Find" format is invalid, then print an error and return
        String[] parts = request.split(":");
        if (parts.length != 2){
            System.out.println("[PEER]: Please enter a file name. Use format \"Find: <File Name>\" or \"Download: <File Name>\"");
            return false;
        }

        // Example: If rquest format is "Find: foo.text", then parts[2] = "foot.txt"
        // Remove any leading or trailing whitespaces from the second part of the request string
        parts[1] = parts[1].trim();

        // Example: If second part of request string has the form "foo.text hello.txt" or "foo   .txt", then print error and return
        if (parts[1].contains(" ")){
            System.out.println("[PEER]: Only one filename at a time can be processed by \"find\" or \"download\". Use format \"Find: <File Name>\" or \"Download: <File Name>\"");
            return false;
        }

        // Example: If second part of request string has the form "foo", then print error and return
        if (!parts[1].contains(".")){
            System.out.println("[PEER]: File extension is possibly missing. Example Find command: \"Find: foo.txt\" or \"Download: foo.txt\"");
            return false;
        }

        return true;
    }

    /**
     * Function for handling join and leave requests
     * @param request: Request string entered by the peer node in the command line.
     */
    public void HandleJoinAndLeave(String request){
        try {
            // Initialize peer IP address and port number
            IP = InetAddress.getLocalHost().getHostAddress();

            Registry registry = LocateRegistry.getRegistry(serverHostname, 8000);
            server = (TrackerInterface) registry.lookup("TrackingServer");

            if (request.toLowerCase().startsWith("join")){
                boolean isJoinSuccess = server.Join(IP, port, machID);
                if (isJoinSuccess){
                    System.out.println("[PEER]: Connected to server at port 8000.");
                    
                    // Update server with all files associated with this client
                    server.UpdateList(fnames, machID);

                    // Register this peer object on the server's registry
                    registry.rebind("Peer_" + machID, this);
                } else {
                    System.out.println("[PEER]: Already part of the tracker server.");
                }
            } else {
                boolean isLeaveSuccess = server.Leave(machID);
                if (isLeaveSuccess){
                    System.out.println("[PEER]: Successfully disconnected from server at port 8000");
                } else {
                    System.out.println("[PEER]: Currently not part of the tracker server. Please enter \"join\" first");
                }
            }
            
        } catch (Exception e){
            System.out.println("[PEER]: It's possible that the server is currently offline. Try joining or leaving later.");
        }
    }

    /**
     * Function for handling find and download requests
     * @param request: Request string entered by the peer node in the command line.
     */
    private static void HandleFindAndDownloadRequest(String request){
        String[] parts = request.split(":");
        String fname = parts[1].trim();
        if (parts[0].trim().equalsIgnoreCase("find")){
            ArrayList<TrackedPeer> answer = null;
            try {
                //answer = server.Find(fname);
                if (answer == null) {  // return since nobody has it
                    System.out.println("[PEER]: No peers found with file");
                    return;
                } else {
                    System.out.println("[PEER]: FOUND FILE");
                }
            } catch (Exception e){
                System.out.println("[PEER]: It's possible that the server is currently offline. Try again later.");
                return;  // return due to exception
            }
        } else {
            //DownloadAsClient(fname);  // calls Find() on tracker, Download() on peer
            // TODO: check result
        }
    }

    /**
     * scans directory according to machine ID
     * populates static structure with info
     * false if OS failures
     */
    private static boolean ScanFiles() {
        try {
            File dir = new File(dirPath);
            for (File f : dir.listFiles()) {
                fnames.add(f.getName());
            }
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    private static boolean ValidateMachID(){
        return (0 <= machID && machID <= 4);
    }

    public static boolean ScanLatencies(String cfname) {
        // read file by lines
        int maxSupported = 10;  // depends on #lines in static config files
        // technically floor(squrt(num_lines))
        List<String> entries = null;
        try {
            entries = Files.readAllLines(Paths.get(cfname));
        } catch (IOException e) {
            return false;
        }
        latencies = new int[maxSupported][maxSupported];

        // make symmetric
        for (int i = 0; i < maxSupported; i++) {
            for (int j = i; j < maxSupported; j++) {
                int latency = Integer.parseInt(
                    entries.get((i * maxSupported) + j));
                latencies[j][i] = latency;
                latencies[i][j] = latency;
            }
        }
        return true;
    }

    public static void main(String[] args) throws RemoteException {
        if (args.length != 2){
            System.out.println("\n[PEER]: Usage: java PeerNode <hostname> <machID>");
            System.out.println("[PEER]: Exiting...");
            System.exit(0);
        }

        try {
            machID = Integer.parseInt(args[1]);

            if (!ValidateMachID()){
                System.out.println("[PEER]: MachID can only have a value 0, 1, 2, 3 or 4");
                System.out.println("[PEER]: Exiting...");
                System.exit(0);
            }
            if (machID < 0){
                throw new RemoteException();
            }
        } catch (Exception e){
            System.out.println("[PEER]: MachID can only have a value 0, 1, 2, 3 or 4.");
            System.exit(0);
        }

        // Save server host name for subsequent peer communication
        serverHostname = args[0];

        // Ensure that each peer is run with its own unique ID
        try {
            Registry registry = LocateRegistry.getRegistry(serverHostname, 8000);
            PeerNodeInterface node = (PeerNodeInterface) registry.lookup("Peer_" + machID);
            node.Ping();
            System.out.println("[PEER]: MachID is currently in use. Please try another machID at runtime.");
            System.exit(0);
        } catch (Exception e){}

        // Join server as soon as node boots up
        port = GetRandomPortNumber();

        dirPath = "files/mach" + machID;
        fnames = new ArrayList<String>();
        if (!ScanFiles()) {
            String msg = "[PEER]: Failed to scan directory. Check that src/files/mach";
            msg += machID + " exists with the correct permissions.";
            System.out.println(msg);
            server.Leave(machID);
            System.exit(0);
        }
        numTasks = new AtomicInteger(0);

        // TODO: Might need to refactor this 
        //Registry registry = LocateRegistry.createRegistry(port);
        // TODO: call ScanLatencies("files/static_latencies.txt")
        //registry.rebind("mach" + machID, this);

        /* 
        try {  // call UpdateList() on tracker
            server.UpdateList(fnames, machID);
        } catch (RemoteException e) {
            // shouldn't really get here if previous calls succeeded
            System.exit(0);
        }*/
        
        PeerNode peer = new PeerNode();
        peer.HandleJoinAndLeave("join");

        // Thread for sending peer requests to the tracking server
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    SendClientRequestToServer(peer);
                }
            }
        }).start();
    }
}