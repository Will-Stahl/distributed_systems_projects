import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.MalformedURLException;

public class TestClient {
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        if (args.length < 1) {
            System.out.print("Please provide a hostname as an argument.\n");
            return;
        }
        Registry registry = LocateRegistry.getRegistry(args[0]);
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.PubSubServer");
        if (Svr.Join("000.000.000.000", 8888)) {
            System.out.print("Joined server.\n");
        }
        else {
            System.out.print("Failed to join server.\n");
        }
    }
}