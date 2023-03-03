import org.junit.*;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;

// The plan: start n servers in a terminal with a bash script
// then switch to different terminal and run these tests

// class tests public facing methods in PubSubServer class
// compile with src appended to classpath
// functions called from client side, must start server process(es) before starting these
public class PublicPubSubMethodsTest extends Thread {

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

    // checks that invalid subscription strings return false
    @Test
    public void CheckInvalidSubscribe() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestD");
        Svr.Join("127.0.0.1", 8001);
        // not a valid topic
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 8001, "Not a topic;orig;org;"));
        // contents field should be blank for Subscribe()
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 8001, ";;;Contents not allowed for Subscribe()"));
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 8001, ";cheems;;Contents not allowed for Subscribe()"));
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 8001, "Science;cheems;memetown;Contents not allowed for Subscribe()"));
        // totally blank not allowed
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 8001, ";;;"));
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 8001, ";;"));  // < 3 ";"
        // attempt with non-Join()'d IP/Port, but valid subscription
        Assert.assertFalse(Svr.Subscribe("127.0.0.1", 7001, "Science;cheems;memetown;"));
        Assert.assertFalse(Svr.Subscribe("127.0.0.2", 7001, "Lifestyle;DaBaby;Urban Rescue Ranch;"));
    }

    // Checks that valid subscription strings return true
    @Test
    public void CheckValidSubscription() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestE");
        Svr.Join("127.0.0.1", 8000);
        // Do all legal combos of subscription fields
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, "Health;;;"));
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, ";doge;;"));
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, ";;The Death Star;"));
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, "Politics;Darth Vader;;"));
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, ";Will Stahl;The Forge;"));
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, "Technology;John Weissman;CSCI 5105;"));
    }

    // sends invalid/nonexistent unsubscription strings/IP/Port
    @Test
    public void CheckInvalidUnsubscribe() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestF");
        Svr.Join("127.0.0.1", 8000);
        Svr.Join("127.0.0.1", 8001);
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8000, "Business;Big Ounce;The Urban Rescue Ranch;"));
        Assert.assertTrue(Svr.Subscribe("127.0.0.1", 8001, "Sports;Big Ounce;The Urban Rescue Ranch;"));
        // unsub from things that you're not subbed to exactly
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, ";Big Ounce;;"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, "Business;;;"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, ";;The Urban Rescue Ranch;"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, "Business;;The Urban Rescue Ranch;"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, ";Quandale;;"));
        // invalid unsub string, including topics
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, "Not a topic;Gort;;"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, ";;;Contents only not allowed"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, ";;;"));  // blank not allowed
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, ";"));  // < 3 ";"
        // A unsub from something that A not subbed to but B is subbed to
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8000, "Sports;Big Ounce;The Urban Rescue Ranch;"));
        // valid unsub string, but nonexistan IP/Port
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.2", 8000, "Business;Big Ounce;The Urban Rescue Ranch;"));
        Assert.assertFalse(Svr.Unsubscribe("127.0.0.1", 8010, "Business;Big Ounce;The Urban Rescue Ranch;"));
    }

    //
    @Test
    public void CheckValidUnsubscribe() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestG");
        Svr.Join("127.0.0.1", 8000);  // two different clients
        Svr.Join("127.0.0.1", 8001);
        // both sub and unsub to same content
        Svr.Subscribe("127.0.0.1", 8000, "Sports;Kevin;The Urban Rescue Ranch;");
        Svr.Subscribe("127.0.0.1", 8001, "Sports;Kevin;The Urban Rescue Ranch;");
        Assert.assertTrue(Svr.Unsubscribe("127.0.0.1", 8000, "Sports;Kevin;The Urban Rescue Ranch;"));
        Assert.assertTrue(Svr.Unsubscribe("127.0.0.1", 8001, "Sports;Kevin;The Urban Rescue Ranch;"));
        // both sub and unsub to same content again
        Svr.Subscribe("127.0.0.1", 8000, "Sports;Kevin;;");
        Svr.Subscribe("127.0.0.1", 8001, "Sports;Kevin;;");
        Assert.assertTrue(Svr.Unsubscribe("127.0.0.1", 8000, "Sports;Kevin;;"));
        Assert.assertTrue(Svr.Unsubscribe("127.0.0.1", 8001, "Sports;Kevin;;"));
    }

    @Test
    public void CheckPing() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestH");
        // default Ping() looks for "server.<ObjectBinding>" at "localhost"
        Assert.assertTrue(Svr.Ping());
    }

    /**
     * "client" listening thread should terminate when it receives
     * any response from server.
     * It sets its private variable to whatever it received
     * This means another client should publish content which does not match
     * AND content that does match.
     */
    @Test
    public void CheckReception()
        throws RemoteException, NotBoundException, InterruptedException {
        
        ClientTestThread client = new ClientTestThread(8000);  // port 8000
        client.start();
        Registry registry = LocateRegistry.getRegistry("127.0.0.1");
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.TestI");
        Svr.Join("127.0.0.1", 8000);  // SERVER Join
        Svr.Join("127.0.0.1", 8001);  // other client will publish
        Svr.Subscribe("127.0.0.1", 8000, "Sports;Bingus;;");
        // yield before publishing
        // this.sleep(500);  // sleep for half second, should be enough time
        Svr.Publish(";Chungus;;client should not receive this."      , "127.0.0.1", 8001);
        Svr.Publish(";Chungus;;client should not receive this."      , "127.0.0.1", 8001);
        Svr.Publish(";Bingus;;client should not receive this."       , "127.0.0.1", 8001);
        Svr.Publish("Sports;Bingus;Pouch;client should receive this.", "127.0.0.1", 8001);
        client.join();  // THREAD join
        Assert.assertEquals(client.getArticle(), "Sports;Bingus;Pouch;client should receive this.");
    }
}