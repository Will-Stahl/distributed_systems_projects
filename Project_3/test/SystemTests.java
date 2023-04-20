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
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


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

    private static boolean startSystem() {
        resetFiles();
        try {  // destroy previous references if exists
            Registry registry = LocateRegistry.getRegistry(11396);
            for (int i = 0; i < 5; i++) {
                try {
                    registry.unbind("Peer_" + i);
                } catch (Exception e) {}
            }
        } catch (Exception e) {}

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
            System.out.println(res);
            Assert.assertNotEquals(
                "[PEER]: no members found with requested file\n", res);
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
     * checks that Find displays new sharer of file
     * checks that Download works even when original holder is down
     */
    @Test
    public void CheckShare() throws IOException {

        PrintWriter writer = new PrintWriter(peers.get(0).getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    peers.get(0).getInputStream()));

        writer.print("download:file2.txt\n");
        writer.flush();

        for (int i = 0; i < 3; i++) {
            String res = reader.readLine();
            System.out.println(res);
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
            System.out.println(res);

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
}


        // BufferedReader tReader = new BufferedReader(new InputStreamReader(
        //             tracker.getInputStream()));  // DEBUG
        // peers.get(0).destroy(); // DEBUG
        // for (int i = 0; i < 15; i++) {  // DEBUG
        //     System.out.println(tReader.readLine());
        // }
