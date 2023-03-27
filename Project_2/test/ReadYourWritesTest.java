import org.junit.*;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;

public class ReadYourWritesTest {

    /**
     * IMPORTANT: each successive test is NOT on a fresh bulletin board.
     * Each test updates the board and assumes the previous test
     * updated it successfully
     */
    private int[] serverToPortMap = new int[]{ -1,2000,2001,2002,2003,2004 };

    // tests interactions with a single server
    @Test
    public void SingleServerInteraction()
            throws RemoteException, NotBoundException, IOException {
        
        int serverNumber = 1;
        int serverPort = serverToPortMap[serverNumber];
        Registry reg = LocateRegistry.getRegistry("localhost", serverPort);
        BulletinBoardServerInterface server = (BulletinBoardServerInterface)
                reg.lookup("BulletinBoardServer_" + serverNumber);

        // article doesn't exist
        Assert.assertEquals("", server.Read());
        Assert.assertFalse(server.Reply("test;article", 1));
        Assert.assertEquals("", server.Choose(1));
        Assert.assertEquals("", server.Choose(0));
        Assert.assertEquals("", server.Choose(-1));

        // now make the article exist
        Assert.assertTrue(server.Publish("test;article"));
        Assert.assertTrue(server.Reply("test;article", 1));
        String cmp = "1.  test;article\n  2.  test;article\n";
        Assert.assertEquals(cmp, server.Read());
        Assert.assertEquals("test;article", server.Choose(1));
    }

  
    // check for expected consistency behaviour across multiple servers
    @Test
    public void ChangeServer()
            throws RemoteException, NotBoundException, IOException {
         
        int serverNumber = 5;  // connect to coordinator
        int serverPort = serverToPortMap[serverNumber];
        Registry reg = LocateRegistry.getRegistry("localhost", serverPort);
        BulletinBoardServerInterface server = (BulletinBoardServerInterface)
                reg.lookup("BulletinBoardServer_" + serverNumber);

        server.Publish("test2;article");  // ID 3

        serverNumber = 4;
        serverPort = serverToPortMap[serverNumber];
        reg = LocateRegistry.getRegistry("localhost", serverPort);
        server = (BulletinBoardServerInterface)
                reg.lookup("BulletinBoardServer_" + serverNumber);  // connect to 4

        server.Publish("test2;article2");  // ID 4
        
        Assert.assertEquals("test2;article", server.Choose(3));
         
        Assert.assertEquals("test2;article2", server.Choose(4));

        // client writes to a server, it must see its writes on from the others
        serverNumber = 3;  // another server, see global order
        serverPort = serverToPortMap[serverNumber];
        reg = LocateRegistry.getRegistry("localhost", serverPort);
        server = (BulletinBoardServerInterface)
                reg.lookup("BulletinBoardServer_" + serverNumber);  // connect to 3
        Assert.assertEquals("test2;article", server.Choose(3));
        Assert.assertEquals("test2;article2", server.Choose(4));

        serverNumber = 1;  // another server, see client's writes
        serverPort = serverToPortMap[serverNumber];
        reg = LocateRegistry.getRegistry("localhost", serverPort);
        server = (BulletinBoardServerInterface)
                reg.lookup("BulletinBoardServer_" + serverNumber);  // connect to 1
        Assert.assertEquals("test2;article", server.Choose(3));
        Assert.assertEquals("test2;article2", server.Choose(4));
    }

}