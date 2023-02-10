import org.junit.*;
import java.util.*;
import java.rmi.RemoteException;
import java.io.IOException;

// TODO: more tests, and bach script to start n servers
// The plan: start n servers in a terminal with a bash script
// then switch to different terminal and run these tests

// class tests public facing methods in PubSubServer class
// compile with src appended to classpath
// functions called from client side, must start server process(es) before starting these
public class PublicPubSubMethodsTest {

    // Check for successful joining, duplication, invalid port/IP
    @Test
    public void CheckJoin() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestA");

        Assert.assertTrue(Join("127.0.0.1", 8000));  // normal successful Join()
        Assert.assertFalse(Join("127.0.0.1", 8000));  // duplicate Join()
        Assert.assertFalse(Join("127.0.0.500", 8000));  // invalid IP
        Assert.assertFalse(Join("127.0.0.1", -1));  // invalid port
        Assert.assertFalse(Join("127.0.0.1", 65535+1));  // invalid port
    }

    // check for filling client capacity (10), defined in PubSubServer
    @Test
    public void CheckServerCapacity() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestB");
        for (int i = 0; i < 10; i++) {
            Join("127.0.0.1", 8000+i);
        }
        Asser.assertFalse(Join("127.0.0.1", 8010));  // server at capacity
    }

    // public static void main(String[] args) throws RemoteException, IOException
    // {
    //     PubSubServer svr = new PubSubServer();
    //     System.out.println("PubSubServer class accessed.");
    // }
}