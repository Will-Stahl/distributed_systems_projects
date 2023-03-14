import org.junit.*;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;
import java.lang.ProcessBuilder;

// The plan: start n servers in a terminal with a bash script
// then switch to different terminal and run these tests

// compile with src appended to classpath
// functions called from client side, must start server process(es) before starting these
public class BulletinBoardTest /*extends Thread*/ {

    // TODO: CheckJoin(), CheckServerCapacity(), and CheckLeave() have been copied from Porj1
    // Check for successful joining, duplication, invalid port/IP
    @Test
    public void CheckJoin() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestA");

        Assert.assertTrue(Svr.Join("127.0.0.1", 8000));  // normal successful Join()
        Assert.assertFalse(Svr.Join("127.0.0.1", 8000));  // duplicate Join()
        Assert.assertFalse(Svr.Join("127.0.0.500", 8000));  // invalid IP
        Assert.assertFalse(Svr.Join("127.0.0.B", 8000));  // invalid IP
        Assert.assertFalse(Svr.Join("127.0.0.1", -1));  // invalid port
        Assert.assertFalse(Svr.Join("127.0.0.1", 65535+1));  // invalid port
    }

    // check for filling client capacity (10), defined in PubSubServer
    @Test
    public void CheckServerCapacity() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestB");
        for (int i = 0; i < 5; i++) {
            Svr.Join("127.0.0.1", 8000+i);
        }
        Assert.assertFalse(Svr.Join("127.0.0.1", 8010));  // server at capacity
    }

    // check that client info is gone after Leave()'ing
    @Test
    public void CheckLeave() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestC");
        Assert.assertFalse(Svr.Leave("127.0.0.1", 8000));  // client hasn't joined yet
        Assert.assertTrue(Svr.Join("127.0.0.1", 8000));
        Assert.assertTrue(Svr.Leave("127.0.0.1", 8000));  // Leave should work for intended purpose
        Assert.assertFalse(Svr.Leave("127.0.0.1", 8000));  // Client info should be gone
    }

    // check stuff
    @Test
    public void CheckSomething() throws RemoteException, NotBoundException {
        // basic code to start server processes
        ArrayList serverProcs = new ArrayList<ProcessBuilder>();
        for (int i = 1; i <= 5; i++) {
            String port = (i + 1999).toString();
            serverProcs.add(new ProcessBuilder("java",
                    "BulletinBoardServer", port, "sequential"));
        }
    }
}