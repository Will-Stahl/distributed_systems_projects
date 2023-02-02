import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class PubSubServer extends UnicastRemoteObject
implements PubSubServerInterface
{
    // TODO: data structures to store client info and contents
    private ArrayList<SubscriberInfo> Subscribers;

    public PubSubServer() throws RemoteException
    {
        Subscribers = new ArrayList<SubscriberInfo>();
    }

    /**
     * Checks if IP address and port number are valid
     * Also checks if client already has Joined
     *
     * @param IP client IP address
     * @param Port client listening at port number
     * @return boolean indicating success
     */
    public boolean Join(String IP, int Port) throws RemoteException
    {
        if (Port > 65535 || Port < 0) {
            System.out.print("Client sent invalid port number\n");
            return false;
        }

        for (int i = 0; i < Subscribers.size(); i++) {
            SubscriberInfo Sub = Subscribers.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                System.out.print("Client already joined\n");
                return false;
            }
        }

        // TODO: check for valid IP address

        Subscribers.add(new SubscriberInfo(IP, Port));
        System.out.printf("Added new client with IP: %s, Port: %d\n", IP, Port);
        return true;
    }

    /**
     * Subscriber should always call Leave() before it terminates
     * Removes calling subscriber from SubscriberInfo list
     *
     * @param IP client IP address
     * @param Port client listening at port number
     * @return boolean indicating success
     */
    public boolean Leave(String IP, int Port) throws RemoteException
    {
        for (int i = 0; i < Subscribers.size(); i++) {
            SubscriberInfo Sub = Subscribers.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                Subscribers.remove(i);
                System.out.print("Removed subscriber\n");
                return true;
            }
        }
        System.out.print("Client was not already joined\n");
        return false;
    }
    
    public boolean Subscribe(String IP, int Port, String Article) throws RemoteException
    {
        if (ArticleValidForSubscribe(Article)){
            //TODO: Fill subscribe details
        } else{
            // TODO: send message to client that article format is invalid
        }
        return false;
    }
    
    public boolean Unsubscribe(String IP, int Port, String Article)
    throws RemoteException
    {
        return false;
    }
    
    public boolean Publish(String Article, String IP, int Port) throws RemoteException
    {
        if (ArticleValidForPublish(Article)){
            //TODO: Fill publish details
        } else{
            // TODO: send message to client that article format is invalid
        }
        return false;
    }

    private static boolean ArticleValidForPublish(String article){
        // Return false if article format is like ";;;contents" or "contents" field is missing
        HashMap<String, String> articleMap = parseArticle(article);
        if (FirstThreeFieldsEmpty(articleMap) || articleMap.get("contents") == "") return false;

        return true;
    }

    private static boolean ArticleValidForSubscribe(String article){
        Set<String> types = new HashSet<>(Arrays.asList("Sports", "Lifestyle", "Entertainment", "Business", "Technology",
                                                        "Science", "Politics" ,"Health"));

        // Check if Type, Originator and Org fields are all empty. 
        // If the aforementioned three fields are not empty, then check if contents field is not empty or if type is invalid
        HashMap<String, String> articleMap = parseArticle(article);
        if (FirstThreeFieldsEmpty(articleMap) || !(types.contains(articleMap.get("type"))) || 
                                    articleMap.get("contents") != "") return false;
        return true;
    }

    private static boolean FirstThreeFieldsEmpty(HashMap<String, String> articleMap){
        return (articleMap.get("type") == "") && 
                (articleMap.get("originator") == "") && 
                (articleMap.get("org") == "");
    }

    private static HashMap<String, String> parseArticle(String article){
        // Remove any leading or trailing white spaces
        article = article.trim();
        LinkedHashMap<String, String> articleMap = new LinkedHashMap<>();
        articleMap.put("type", "");
        articleMap.put("originator", "");
        articleMap.put("org", "");
        articleMap.put("contents", "");

        ArrayList<String> keys = new ArrayList<String>(articleMap.keySet());

        final char SEPARATOR = ';';
        String value = "";
        int keyIndex = 0;
        for (int i = 0; i < article.length(); i++){
            if (article.charAt(i) == SEPARATOR){
                articleMap.put(keys.get(keyIndex), value.trim());
                keyIndex += 1;
                value = "";
            } else if (i == article.length() - 1){
                value += article.charAt(i);
                articleMap.put(keys.get(keyIndex), value);
            } else {
                value += article.charAt(i);
            }
        }
        return articleMap;
    }
    
    public boolean Ping() throws RemoteException
    {
        return true;
    }

    public static void main(String args[])
    throws RemoteException, MalformedURLException
    {
        LocateRegistry.createRegistry(1099);
        PubSubServerInterface ContentSrv = new PubSubServer();
        Naming.rebind("server.PubSubServer", ContentSrv);
        System.out.println("Publish-Subscribe Server is ready.");
    }

    private static boolean IsValidIPAddress(String IP) {
        String[] parts = IP.split("\\.");

        if (parts.length != 4) return false;

        for (int i = 0; i < parts.length; i++){
            String part = parts[i];
            if (Integer.parseInt(part) < 0 || Integer.parseInt(part) > 255) return false;
        }
        return true;
    }
}