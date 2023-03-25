import java.util.*;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.io.IOException;


public class StartSystem {
    private ArrayList<Process> procs;

    private boolean startServers(String consistency) throws IOException {
        procs = new ArrayList<Process>();
        for (int i = 4; i >= 0; i--) {  // start corrdinator first
            String port = Integer.toString(i + 2000);
            Process p = new ProcessBuilder("java", "-cp", "../src",
                    "BulletinBoardServer", "localhost", port, consistency).start();
            if (!p.isAlive()) {
                killSevers();
                return false;
            }
            procs.add(p);
        }
        return true;
    }

    private void killSevers() {
        for (Process p : procs) {
            p.destroy();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java StartSystem <sequential|quorum|readyourwrites>");
            System.exit(0);
        }
        if (!args[0].equals("sequential") && !args[0].equals("quorum")
            && !args[0].equals("readyourwrites")) {
            System.out.println("Usage: java StartSystem <sequential|quorum|readyourwrites>");
            System.exit(0);
        }

        StartSystem billBoard = new StartSystem();
        if (!billBoard.startServers(args[0])) {
            System.out.println("Failed to start a server. Aborting. Please try again later.");
            System.exit(0);
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter anything to shut down the Bulletin Board System.");
        sc.nextLine();
        billBoard.killSevers();
    }
}