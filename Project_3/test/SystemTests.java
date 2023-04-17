import org.junit.*;
import java.util.*;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;


public class SystemTests {
    private static ArrayList<Process> peers;
    private static Process tracker;

    public SystemTests() {
        peers = new ArrayList<Process>();
        if (!startSystem()) {
            System.out.println("processes failed to start, tests will fail");
        }

    }

    private static boolean startSystem() {
        try {
            // start tracker first
            tracker = new ProcessBuilder("java", "-cp", "../src",
                    "Tracker").start();
            if (!tracker.isAlive()) {
                killSystem();
                return false;
            }

            // start peers after
            for (int i = 0; i < 5; i++) {
                Process p = new ProcessBuilder("java", "-cp", "../src",
                    "PeerNode", "localhost", String.valueOf(i)).start();
                if (!p.isAlive()) {
                    killSystem();
                    return false;
                }
                peers.add(p);
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static void killSystem() {
        for (Process p : peers) {
            p.destroy();
        }
        tracker.destroy();
    }

    // IMPORTANT: assumes system running locally
    // resets directories to only have original file
    private static void resetFiles(String request) {
        String filePath = "../src/files";
        for (int i = 0; i < 10; i++) {
            String path = filePath + "/mach" + i;
            File dir = new File(path);
            for (File f : dir.listFiles()) {
                if (f.getName().equals("file" + i + ".txt")) {
                    continue;  // keep machi's default file
                }
                f.delete();
            }
        }
    }

    @Test
    public void TestName() throws InterruptedException {
        System.out.println("test test antered");
        PrintWriter writer = new PrintWriter(peers.get(0).getOutputStream());
        writer.print("try something\n");
        writer.print("try something\n");
        writer.print("try something\n");
        writer.flush();
        writer.close();
        Thread.sleep(500);
        Scanner reader = new Scanner(peers.get(0).getInputStream());
        while (reader.hasNextLine()) {
            System.out.println(reader.nextLine());
        }

        killSystem();
    }
}