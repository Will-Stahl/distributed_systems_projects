import org.junit.*;
import java.util.*;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;


public class SystemTests {
    private static ArrayList<Process> peers;
    private static Process tracker;
    private static Thread stopper;

    // called for every test
    public SystemTests() {
        peers = new ArrayList<Process>();
        if (!startSystem()) {
            System.out.println("processes failed to start, tests will fail");
        }

        stopper = new Thread(new Runnable(){
            @Override
            public void run(){
                Scanner ender = new Scanner(System.in);
                ender.nextLine();
                killSystem();
            }
        }).start();

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

    /**
     * basic tests for Find() on a vanilla system
     */
    @Test
    public void TestFind() throws InterruptedException, IOException {
        PrintWriter writer = new PrintWriter(peers.get(0).getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    peers.get(0).getInputStream()));
        String ln;
        if (!tracker.isAlive()) {  // some reason this makes it work
            System.out.println("Tracker not up");
        }

        writer.print("find:file0.txt\n");
        writer.flush();
        for (int i = 0; i < 3; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 0", reader.readLine());

        writer.print("find:file1.txt\n");
        writer.flush();
        // for (int i = 0; i < 1; i++) {
        //     reader.readLine();
        // }
        Assert.assertEquals("[PEER]: Found file at nodes: 1", reader.readLine());

        writer.print("find:file2.txt\n");
        writer.flush();
        for (int i = 0; i < 1; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 2", reader.readLine());

        writer.print("find:file3.txt\n");
        writer.flush();
        for (int i = 0; i < 1; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 3", reader.readLine());

        writer.print("find:file4.txt\n");
        writer.flush();
        for (int i = 0; i < 1; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 4", reader.readLine());

        killSystem();
    }
}