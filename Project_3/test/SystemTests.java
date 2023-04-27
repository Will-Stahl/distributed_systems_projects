import org.junit.*;
import java.util.*;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * please use killSystem() at the end of EVERY test
 */
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
    }

    private static void clearRegistry() {
        try {  // destroy previous references if exists
            Registry registry = LocateRegistry.getRegistry(11396);
            for (int i = 0; i < 5; i++) {
                try {
                    registry.unbind("Peer_" + i);
                } catch (Exception e) {}
            }
            registry.unbind("TrackingServer");
        } catch (Exception e) {}

    }

    private static boolean startSystem() {
        resetFiles();
        clearRegistry();

        try {
            // start tracker first
            tracker = new ProcessBuilder("java", "-cp", "../src",
                "Tracker").start();
            BufferedReader tReader = new BufferedReader(new InputStreamReader(
                tracker.getInputStream()));
            tReader.readLine();  // wait for output to indicate readiness
            if (!tracker.isAlive()) {
                killSystem();
                return false;
            }

            // start peers after
            for (int i = 0; i < 5; i++) {
                Process p = new ProcessBuilder("java", "-cp", "../src",
                    "PeerNode", "localhost", String.valueOf(i)).start();
                peers.add(p);
            }
            for (Process p : peers) {  // this waits until they have started
                if (!p.isAlive()) {
                    killSystem();
                    return false;
                }
                // wait for each to produce output to ensure they have started
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                    peers.get(peers.indexOf(p)).getInputStream()));
                reader.readLine();
                reader.readLine();
                reader.readLine();
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
    private static void resetFiles() {
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
    public void TestFind() throws IOException, InterruptedException {
        PrintWriter writer = new PrintWriter(peers.get(0).getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    peers.get(0).getInputStream()));
        String ln;

        writer.print("find:file0.txt\n");
        writer.flush();

        Assert.assertEquals("[PEER]: Found file at nodes: 0", reader.readLine());

        writer.print("find:file1.txt\n");
        writer.flush();
        for (int i = 0; i < 3; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 1", reader.readLine());

        writer.print("find:file2.txt\n");
        writer.flush();
        for (int i = 0; i < 3; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 2", reader.readLine());

        writer.print("find:file3.txt\n");
        writer.flush();
        for (int i = 0; i < 3; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 3", reader.readLine());

        writer.print("find:file4.txt\n");
        writer.flush();
        for (int i = 0; i < 3; i++) {
            reader.readLine();
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 4", reader.readLine());

        killSystem();
    }

    /**
     * basic download test that checks folder for whether file was downloaded
     * calls resetFiles() to ensure clear file system
     */
    @Test
    public void TestDownload() throws IOException, InterruptedException {

        PrintWriter writer = new PrintWriter(peers.get(0).getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    peers.get(0).getInputStream()));

        writer.print("download:file4.txt\n");
        writer.flush();

        for (int i = 0; i < 3; i++) {
            String res = reader.readLine();
            System.out.println("TestDownload: " + res);
            Assert.assertNotEquals(
                "[PEER]: No peers found with file\n", res);
        }  // consume output to allow execution of download

        String contentsA, contentsB;
        contentsA = null;
        contentsB = null;
        try {

            contentsB = new String(Files.readAllBytes(Paths.get(
                    "../src/files/mach4/file4.txt")));  // read from original dir
            contentsA = new String(Files.readAllBytes(Paths.get(
                    "../src/files/mach0/file4.txt")));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertEquals(contentsB, contentsA);

        killSystem();
    }

    /**
     * checks that files are shareable from peer after downloading
     * checks that Download works even when original holder is down
     */
    @Test
    public void TestShare() throws IOException {

        PrintWriter writer = new PrintWriter(peers.get(0).getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    peers.get(0).getInputStream()));

        writer.print("download:file2.txt\n");
        writer.flush();

        for (int i = 0; i < 3; i++) {
            String res = reader.readLine();
            System.out.println("TestShare: " + res);
            Assert.assertNotEquals(
                "[PEER]: no members found with requested file\n", res);
            if (res.equals(
                "[PEER]: downloaded file2.txt\n")) {
                break;
            }
        }  // consume output to allow execution of download

        reader = new BufferedReader(new InputStreamReader(
                    peers.get(1).getInputStream()));  // peer 1 looks for file2.txt
        writer = new PrintWriter(peers.get(1).getOutputStream());
        peers.get(2).destroy();  // only leave peer 0 to share this
        // tries to download from peer 2 and fails, tries next
        writer.print("download:file2.txt\n");
        writer.flush();

        for (int i = 0; i < 3; i++) {
            String res = reader.readLine();
            System.out.println("TestShare: " + res);

            Assert.assertNotEquals(
                "[PEER]: no members found with requested file\n", res);
            if (res.equals(
                "[PEER]: downloaded file2.txt\n")) {
                break;
            }
        }  // consume output to allow execution of download

        String contentsA, contentsB;
        contentsA = new String(Files.readAllBytes(Paths.get(
                "../src/files/mach2/file2.txt")));  // read from original dir
        contentsB = new String(Files.readAllBytes(Paths.get(
                "../src/files/mach1/file2.txt")));  // should be in this dir
        Assert.assertEquals(contentsB, contentsA);

        killSystem();
    }

    /**
     * kills and restarts tracker
     * checks that nodes can rejoin smoothly
     * checks that files are findable after rejoining
     */
    @Test
    public void TestTrackerFault() throws IOException {
        tracker.destroy();
        clearRegistry();
        tracker = new ProcessBuilder("java", "-cp", "../src",
                "Tracker").start();
        BufferedReader tReader = new BufferedReader(new InputStreamReader(
                tracker.getInputStream()));
        Assert.assertTrue(tracker.isAlive());
        // wait for output to indicate readiness
        tReader.readLine();  // blank line
        Assert.assertEquals("[SERVER]: Tracking Server is ready at port 11396.", tReader.readLine());

        PrintWriter writer = null;
        for (int i = 0; i < 5; i++) {
            writer = new PrintWriter(peers.get(i).getOutputStream());
            writer.print("join\n");
            writer.flush();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
            peers.get(4).getInputStream()));
        writer.print("find:file2.txt\n");
        writer.flush();
        Assert.assertEquals("[PEER]: Connected to server at port 11396",
            reader.readLine());
        for (int i = 0; i < 3; i++) {
            System.out.println("TestTrackerFault: " + reader.readLine());
        }
        Assert.assertEquals("[PEER]: Found file at nodes: 2", reader.readLine());

        killSystem();
    }

    /**
     * Kills and restarts peers
     * checks that they can rejoin smoothly and share files
     * also checks that dead peer
     */
    @Test
    public void TestPeerFault() throws IOException, InterruptedException {
        resetFiles();
        peers.get(0).destroy();  // destroy then restart
        peers.get(1).destroy();  // destroy then see its files inaccessible

        // restart peer
        peers.set(0, new ProcessBuilder("java", "-cp", "../src",
            "PeerNode", "localhost", String.valueOf(0)).start());
        Assert.assertTrue(peers.get(0).isAlive());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            peers.get(0).getInputStream()));
        String res = reader.readLine();
        Assert.assertTrue(
            res.equals("[PEER]: Already part of the tracker server.")
            || res.equals("[PEER]: Connected to server at port 11396"));
        // tracker may or may not boot peer's info

        reader = new BufferedReader(new InputStreamReader(
            peers.get(4).getInputStream()));
        PrintWriter writer = new PrintWriter(peers.get(4).getOutputStream());
        writer.print("find:file0.txt\n");
        writer.flush();

        Assert.assertEquals("[PEER]: Found file at nodes: 0",
            reader.readLine());

        Thread.sleep(2000);  // give tracker enough time to remove peer
        writer.print("find:file1.txt\n");
        writer.flush();  // check that tracker removed dead peer's info
        System.out.println("TestPeerFault: " + reader.readLine());
        System.out.println("TestPeerFault: " + reader.readLine());
        System.out.println("TestPeerFault: " + reader.readLine());
        Assert.assertEquals("[PEER]: No peers found with file",
            reader.readLine());

        killSystem();
    }


    /**
     * checks that peers chosen according to latency if no additional load
     */
    @Test
    public void TestLatencyChoice() throws IOException {
        resetFiles();

        PrintWriter writer = null;
        // setup for peers 1, 2, 3, 4 to have file4.txt
        for (int i = 1; i < 4; i++) {
            writer = new PrintWriter(peers.get(i).getOutputStream());
            writer.print("download:file4.txt\n");
            writer.flush();
        }
        // wait till all confirm download, as to eliminate load differences
        BufferedReader reader = null;
        for (int i = 1; i < 4; i++) {
            reader = new BufferedReader(new InputStreamReader(
                peers.get(i).getInputStream()));
            reader.readLine();
        }
        // peer 0 will choose
        writer = new PrintWriter(peers.get(0).getOutputStream());
        writer.print("download:file4.txt\n");
        writer.flush();
        reader = new BufferedReader(new InputStreamReader(
            peers.get(0).getInputStream()));
        reader.readLine();

        // read from peer 4
        reader = new BufferedReader(new InputStreamReader(
            peers.get(4).getInputStream()));
        for (int i = 0; i < 3; i++) {
            System.out.println("LatencyChoice: " + reader.readLine());
            reader.readLine();
        }
        // peer 4 has lowest latency to peer 0 according to static_letency.txt
        Assert.assertEquals("[PEER]: Serviced file to peer: 0", reader.readLine());

        killSystem();
    }


    /**
     * checks that peer is chosen according to load
     */
    @Test
    public void TestPeerChoice() throws IOException {
        resetFiles();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
            peers.get(4).getInputStream()));
        PrintWriter writer = new PrintWriter(peers.get(4).getOutputStream());
        writer.print("download:file3.txt\n");
        writer.flush();

        System.out.println("TestPeerChoice: " + reader.readLine());

        // setup such that peer 4 is servicing 3 downloads simultaneously
        for (int i = 1; i < 4; i++) {
            writer = new PrintWriter(peers.get(i).getOutputStream());
            writer.print("download:file4.txt\n");
            writer.flush();
        }

        // peer 0
        writer = new PrintWriter(peers.get(0).getOutputStream());
        writer.print("download:file3.txt\n");  // choice between peer 3 and 4
        writer.flush();

        reader = new BufferedReader(new InputStreamReader(
            peers.get(0).getInputStream()));
        for (int i = 0; i < 3; i++) {
            System.out.println("TestPeerChoice: " + reader.readLine());
        }

        // see that peer 4 did not service the download
        reader = new BufferedReader(new InputStreamReader(
            peers.get(4).getInputStream()));
        while (reader.ready()) {
            Assert.assertNotEquals("[PEER]: Serviced file to peer: 0",
                reader.readLine());
        }

        // see that peer 3 served the download
        reader = new BufferedReader(new InputStreamReader(
            peers.get(3).getInputStream()));
        boolean serviced = false;
        while (reader.ready()) {
            if (reader.readLine().equals("[PEER]: Serviced file to peer: 0")) {
                serviced = true;
                break;
            }
        }
        Assert.assertTrue(serviced);
    
        killSystem();
    }

    /**
     * checks that simultaneous downloads complete successfully
     */
    @Test
    public void TestSimultaneousDownloads() throws IOException {
        resetFiles();

        PrintWriter writer = null;
        // all other peers download from peer 4
        for (int i = 0; i < 4; i++) {
            writer = new PrintWriter(peers.get(i).getOutputStream());
            writer.print("download:file4.txt\n");
            writer.flush();
        }
        // wait till all confirm download
        BufferedReader reader = null;
        for (int i = 0; i < 4; i++) {
            reader = new BufferedReader(new InputStreamReader(
                peers.get(i).getInputStream()));
            reader.readLine();
        }

        String contentsFour = new String(Files.readAllBytes(Paths.get(
            "../src/files/mach4/file4.txt")));  // read from original dir

        // check that all machines downloaded correctly
        String contentsVar = new String(Files.readAllBytes(Paths.get(
            "../src/files/mach0/file4.txt")));
        Assert.assertEquals(contentsFour, contentsVar);

        contentsVar = new String(Files.readAllBytes(Paths.get(
            "../src/files/mach1/file4.txt")));
        Assert.assertEquals(contentsFour, contentsVar);

        contentsVar = new String(Files.readAllBytes(Paths.get(
            "../src/files/mach2/file4.txt")));
        Assert.assertEquals(contentsFour, contentsVar);

        contentsVar = new String(Files.readAllBytes(Paths.get(
            "../src/files/mach3/file4.txt")));
        Assert.assertEquals(contentsFour, contentsVar);

        killSystem();
    }
}
