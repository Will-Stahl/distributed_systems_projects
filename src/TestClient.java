import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.MalformedURLException;

import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        if (args.length < 1) {
            System.out.print("Please provide a hostname as an argument.\n");
            return;
        }
        Registry registry = LocateRegistry.getRegistry(args[0]);
        PubSubServerInterface Svr = (PubSubServerInterface) registry.lookup("server.PubSubServer");

        Scanner Input = new Scanner(System.in);
        System.out.print("Type \'n\' and Enter to continue.\n");
        while (!Input.nextLine().equals("n")) {
            System.out.print("Type \'n\' and Enter to continue.\n");
        }

        try {
            Svr.Ping();
        }
        catch(RemoteException e) {
            System.out.print("Server is down. Exiting now.\n");
            return;
        }
        System.out.print("Server is up.\n");

        if (Svr.Join("000.000.000.000", 8888)) {
            System.out.print("Joined server.\n");
        } else {
            System.out.print("Failed to join server.\n");
        }

        if (Svr.Leave("000.000.000.000", 8888)) {
            System.out.print("Successfully left server.\n");
        } else {
            System.out.print("Failed to leave server.\n");
        }

    }
}