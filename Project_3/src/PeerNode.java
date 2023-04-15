import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PeerNode extends UnicastRemoteObject implements PeerNodeInterface {
    private static String IP;
    private static int port;
    private static int machID;
    private static ArrayList<String> fnames;
    private static TrackerInterface server;
    private static String serverHostname;

    public PeerNode() throws RemoteException {}
    
    public int GetLoad() throws RemoteException {
        return 0;
    }

    private static String Download(String fname) {
        return "";
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

    private static void SendClientRequestToServer(){
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("\n[PEER]: Enter command: ");
            String request = sc.nextLine();
            
            if (request.equalsIgnoreCase("join") || request.equalsIgnoreCase("leave")){
                HandleJoinAndLeave(request);
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
    private static void HandleJoinAndLeave(String request){
        try {
            // Initialize peer IP address and port number
            IP = InetAddress.getLocalHost().getHostAddress();

            Registry registry = LocateRegistry.getRegistry(serverHostname, 8000);
            server = (TrackerInterface) registry.lookup("TrackingServer");

            if (request.toLowerCase().startsWith("join")){
                boolean isJoinSuccess = server.Join(IP, port, machID);
                if (isJoinSuccess){
                    System.out.println("[PEER]: Connected to server at port 8000.");
                } else {
                    throw new RemoteException();
                }
            } else {
                boolean isLeaveSuccess = server.Leave(machID);
                if (isLeaveSuccess){
                    System.out.println("[PEER]: Successfully disconnected from server at port 8000");
                } else {
                    throw new RemoteException();
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
            try {
                server.Find(fname);
                // TODO: check if returns null, where file is inaccessible
                System.out.println("[PEER]: FOUND FILE");
            } catch (Exception e){
                System.out.println("[PEER]: It's possible that the server is currently offline. Try again later.");
            }
        } else {
            Download(fname);
        }
    }

    /**
     * scans directory according to machine ID
     * populates static structure with info
     * false if OS failures
     */
    private static boolean ScanFiles() {
        try {
            File dir = new File("files/mach" + machID);
            for (File f : dir.listFiles()) {
                fnames.add(f.getName());
            }
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("\n[PEER]: Usage: java PeerNode <hostname> <machID>");
            System.out.println("[PEER]: Exiting...");
            System.exit(0);
        }

        try {
            machID = Integer.parseInt(args[1]);
            if (machID < 0){
                throw new RemoteException();
            }
        } catch (Exception e){
            System.out.println("[PEER]: Mach ID must be a number greater than or equal to 0.");
            System.exit(0);
        }

        // Join server as soon as node boots up
        serverHostname = args[0];
        port = GetRandomPortNumber();
        HandleJoinAndLeave("join");

        // Thread for sending peer requests to the tracking server
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    SendClientRequestToServer();
                }
            }
        }).start();
        /* 
        fnames = new ArrayList<String>();
        if (!ScanFiles()) {
            String msg = "[PEER]: Failed to scan directory. Check that src/files/mach";
            msg += machID + " exists with the correct permissions.";
            System.out.println(msg);
            System.exit(0);
        }*/
    }
}