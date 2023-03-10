import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class ReadYourWritesStrategy implements ConsistencyStrategy {
    public boolean ServerPublish(String article) {
        return false;
    }

    public String ServerRead() {
        return "";
    }

    public String ServerChoose(Integer articleID) {
        return "";
    }

    public boolean ServerReply(String article) {
        return false;
    }
}