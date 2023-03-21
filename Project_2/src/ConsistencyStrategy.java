import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public interface ConsistencyStrategy
{
    /**
     *
     */
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer);

    /**
     * based on the used consistency/strategy, this method must determine
     * whether it contacts other servers (quorum) or just uses its local copy
     */
    public String ServerRead(BulletinBoardServer selfServer);
    public String ServerChoose(int articleID, ReferencedTree contentTree);
}