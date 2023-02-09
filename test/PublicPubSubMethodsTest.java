import org.junit.*;
import java.util.*;
import java.rmi.RemoteException;
import java.io.IOException;

// class tests public facing methods in PubSubServer class
// compile with src appended to classpath
public class PublicPubSubMethodsTest {

    // Check 
    @Test
    public void CheckJoin(){
        PubSubServerInterrface svr = new PubSubServer();
        // TODO: check for successful joining, capacity, duplication, invalid port/IP, 
        Assert.assertTrue(true);
    }

    // public static void main(String[] args) throws RemoteException, IOException
    // {
    //     PubSubServer svr = new PubSubServer();
    //     System.out.println("PubSubServer class accessed.");
    // }
}