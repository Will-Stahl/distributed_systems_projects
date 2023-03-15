import org.junit.*;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;
import java.lang.ProcessBuilder;


// compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunTestClass.java 
// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass
// compile with src appended to classpath
// each test starts and kills its own server processes

public class BulletinBoardTest /*extends Thread*/ {

    // TODO: CheckJoin(), CheckServerCapacity(), and CheckLeave() have been copied from Proj1
    // Check for successful joining, duplication, invalid port/IP
    // @Test
    // public void CheckJoin() throws RemoteException, NotBoundException {
    //     Registry registry = LocateRegistry.getRegistry("127.0.0.1");
    //     BulletinBoardServerInterface Svr = (BulletinBoardServerInterface) registry.lookup("server.TestA");

    //     Assert.assertTrue(Svr.Join("127.0.0.1", 8000));  // normal successful Join()
    //     Assert.assertFalse(Svr.Join("127.0.0.1", 8000));  // duplicate Join()
    //     Assert.assertFalse(Svr.Join("127.0.0.500", 8000));  // invalid IP
    //     Assert.assertFalse(Svr.Join("127.0.0.B", 8000));  // invalid IP
    //     Assert.assertFalse(Svr.Join("127.0.0.1", -1));  // invalid port
    //     Assert.assertFalse(Svr.Join("127.0.0.1", 65535+1));  // invalid port
    // }

    // // check for filling client capacity (10), defined in PubSubServer
    // @Test
    // public void CheckServerCapacity() throws RemoteException, NotBoundException {
    //     Registry registry = LocateRegistry.getRegistry("127.0.0.1");
    //     BulletinBoardServer Svr = (BulletinBoardServerInterface) registry.lookup("server.TestB");
    //     for (int i = 0; i < 5; i++) {
    //         Svr.Join("127.0.0.1", 8000+i);
    //     }
    //     Assert.assertFalse(Svr.Join("127.0.0.1", 8010));  // server at capacity
    // }

    // // check that client info is gone after Leave()'ing
    // @Test
    // public void CheckLeave() throws RemoteException, NotBoundException {
    //     Registry registry = LocateRegistry.getRegistry("127.0.0.1");
    //     BulletinBoardServerInterface Svr = (BulletinBoardServerInterface) registry.lookup("server.TestC");
    //     Assert.assertFalse(Svr.Leave("127.0.0.1", 8000));  // client hasn't joined yet
    //     Assert.assertTrue(Svr.Join("127.0.0.1", 8000));
    //     Assert.assertTrue(Svr.Leave("127.0.0.1", 8000));  // Leave should work for intended purpose
    //     Assert.assertFalse(Svr.Leave("127.0.0.1", 8000));  // Client info should be gone
    // }

    // check stuff
    @Test
    public void CheckSomething()
            throws RemoteException, NotBoundException, IOException {
        // basic code to start server processes
        ArrayList<Process> serverProcs = startServers("sequential");
        int[] serverToPortMap = generateMap();
        
        int serverNumber = 1;
        int serverPort = serverToPortMap[serverNumber];
        System.out.println("C");
        Registry reg = LocateRegistry.getRegistry("localhost", serverPort);
        System.out.println("B");
        BulletinBoardServerInterface server = (BulletinBoardServerInterface)
                reg.lookup("BulletinBoardServer_" + serverNumber);
        
        System.out.println("A");
        Assert.assertTrue(server.Publish("testarticle"));
        System.out.println(server.Read());

        killSevers(serverProcs);
    }

    private int[] generateMap() {
        // use server's number as index, so index from 1, not 0
        return new int[]{ -1,2000,2001,2002,2003,2004 }; 
    }

    private ArrayList<Process> startServers(String consistency) throws IOException {
        ArrayList<Process> procs = new ArrayList<Process>();
        for (int i = 0; i < 5; i++) {
            String port = Integer.toString(i + 2000);
            procs.add(new ProcessBuilder("java",
                    "../src/BulletinBoardServer", port, consistency).start());
        }
        return procs;
    }

    private void killSevers(ArrayList<Process> procs) {
        for (int i = 0; i < 5; i++) {
            procs.get(i).destroy();
        }
    }
}