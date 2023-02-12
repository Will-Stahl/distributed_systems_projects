import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.*;

public class PubSubServer extends UnicastRemoteObject implements PubSubServerInterface
{
    private ArrayList<SubscriberInfo> Subscribers;
    // subMap maps a list of subscribers to keys of subscription fields
    private HashMap<String, ArrayList<SubscriberInfo>> subMap;
    private static DatagramSocket socket;
    private final int MAX_CLIENTS = 5;
    private static int clientCount = 0;
    private static Set<Integer> portsCurrentlyInUse = new HashSet<>();

    public PubSubServer() throws RemoteException, IOException
    {
        Subscribers = new ArrayList<SubscriberInfo>();
        subMap = new HashMap<>();
        socket = new DatagramSocket();
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
        // In the off chance that the random port generated is currently in use, we should prompt the user to try joining again.
        if (portsCurrentlyInUse.contains(Port)){
            System.out.println("[SERVER]: This port is currently in use. Please try joining again to attempt connecting to a different port.");
            return false;
        }

        // If server is at max capacity, then prompt user to try joining again at a later time.
        if (clientCount == MAX_CLIENTS) {

            System.out.println("[SERVER]: Server Capacity reached! Please try joining again later.");
            return false;
        }

        if (Port > 65535 || Port < 0) {
            System.out.println("[SERVER]: Client port number is invalid and cannot be used for communication.");
            return false;
        }

        for (int i = 0; i < Subscribers.size(); i++) {
            SubscriberInfo Sub = Subscribers.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                System.out.println("[SERVER]: Client is already part of the group server.");
                return false;
            }
        }

        // check for valid IP address
        if (IsValidIPAddress(IP)){
            Subscribers.add(new SubscriberInfo(IP, Port));
            System.out.printf("[SERVER]: Added new client with IP: %s, Port: %d\n", IP, Port);
            clientCount += 1;
            return true;
        }
        System.out.println("[SERVER]: Invalid IP Address");
        return false;
    }

    private static boolean IsValidIPAddress(String IP) {
        String[] parts = IP.split("\\.");

        if (parts.length != 4) return false;

        for (int i = 0; i < parts.length; i++){
            String part = parts[i];
            try {
                if (Integer.parseInt(part) < 0
                    || Integer.parseInt(part) > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            } 
        }
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
        // check for subscriber in Subscribers
        SubscriberInfo subPtr = null;
        for (int i = 0; i < Subscribers.size(); i++) {
            SubscriberInfo Sub = Subscribers.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                subPtr = Subscribers.get(i);
                Subscribers.remove(i);
            }
        }
        if (subPtr == null) {
            System.out.printf("[SERVER]: Client at IP Address %s is and Port %d is not currently part of the server.\n", IP, Port);
            return false;
        }
        // Remove client from all subMap'ings as well
        // TODO: test that clients are removed from subMap
        final SubscriberInfo fnlSubPtr = subPtr;
        subMap.forEach((k, v) -> {
                if (v.contains(fnlSubPtr)) {
                    v.remove(fnlSubPtr);
            }});
        
        clientCount -= 1;
        System.out.printf("[SERVER]: Removed client at address %s and Port %d.\n", IP, Port);
        return true;
    }
    
    /**
     * Add article to list of subscriptions if it has already not been created earlier and 
     * add client to list of subscriptions if the article already exists. If the article format
     * is not valid for subscription, then return an error message.
     * 
     * @param IP: Client IP address
     * @param Port: Client listening at port number
     * @param Article: Article name the client wants to subscribe to
     */
    public boolean Subscribe(String IP, int Port, String Article) throws RemoteException
    {
        if (!ArticleValidForSubscribeOrUnSub(Article)){
            System.out.println("[SERVER]: Article type not valid for subscribing.");
            return false;
        }

        // If article has not been created earlier, then we should create it
        HashMap<String, String> subDetailsMap = parseArticle(Article);
        subDetailsMap.remove("contents");  // don't put a blank field in

        String subscriptionDetails = unparseSubscription(subDetailsMap);
        if (!subMap.containsKey(subscriptionDetails)){
            subMap.put(subscriptionDetails, new ArrayList<SubscriberInfo>());
        }

        // Add current client to the article subscriber list
        for (SubscriberInfo sub : Subscribers){
            if (sub.GetIP().equals(IP) && sub.GetPort() == Port){
                if (subMap.get(subscriptionDetails).contains(sub)) {
                    // may not need to return false here, but don't duplicate subscriptions
                    System.out.println("[SERVER]: Client has already subscribed to this article in the past.");
                    return false;
                }
                subMap.get(subscriptionDetails).add(sub);
                System.out.printf("[SERVER]: Client at Port Number %d has subscribed to Article: \"%s\".\n", Port, Article);
                return true;
            }
        }
        System.out.println("[SERVER]: Client's IP address and Port no. not found on server.");
        return false;
    }
    
    /**
     * Unsubscribe client from an article if the article exists. If the article doesn't exist,
     * then just return an error message.
     * 
     * @param IP: Client IP address
     * @param Port: Client listening at port number
     * @param Article: Article name the client wants to unsubscribe from
     */
    public boolean Unsubscribe(String IP, int Port, String Article) throws RemoteException
    {
        if (!ArticleValidForSubscribeOrUnSub(Article)){
            System.out.println("[SERVER]: Article type not valid for unsubscribing.");
            return false;
        }

        HashMap<String, String> subDetailsMap = parseArticle(Article);
        subDetailsMap.remove("contents"); 
        String subscriptionDetails = unparseSubscription(subDetailsMap);
        if (!subMap.containsKey(subscriptionDetails)){
            // If the article hasn't been published earlier, then return false
            System.out.printf("[SERVER]: Article \"%s\" does not exist and cannot be unsubscribed from.\n", Article);
            return false;
        }

        // Get all subscribers to the current article and remove the client
        ArrayList<SubscriberInfo> leavingFrom = subMap.get(subscriptionDetails);
        for(int i = 0; i < leavingFrom.size(); i++){
            SubscriberInfo sub = leavingFrom.get(i);
            if (sub.GetIP().equals(IP) && sub.GetPort() == Port){
                System.out.printf("[SERVER]: Client with IP Address %s has unsubscribed from Article \"%s\".\n",IP, Article);
                leavingFrom.remove(i);
                return true;
            }
        }
        System.out.printf("[SERVER]: Client with IP Address %s is not currently subscribed to Article \"%s\".", IP, Article);
        return false;
    }
    
    /**
     * Publish article to a list of clients if it has not already been published before
     * and that has the valid format required for publishing.
     * 
     * @param IP: Client IP address
     * @param Port: Client listening at port number
     * @param Article: Article name that should be published
     */
    public boolean Publish(String Article, String IP, int Port) throws RemoteException
    {
        boolean publisherJoined = false;
        // clients must Join() before they publish
        for (SubscriberInfo sub : Subscribers) {
            if (sub.GetIP().equals(IP) && sub.GetPort() == Port) {
                publisherJoined = true;
            }
        }
        if (!publisherJoined) {
            System.out.println("[SERVER]: Client must Join() in order to Publish() content.");
            return false;
        }
            
        if (!ArticleValidForPublish(Article)){
            System.out.println("[SERVER]: Article format not valid for publishing.");
            return false;
        }
        // list containing subscription fields combination from Article
        // as well as all subcombinations of those fields
        ArrayList<String> comboList = genLessSpecificSubs(Article);
        // maintain list of clients who have already had this published to them
        ArrayList<SubscriberInfo> sentToAlready = new ArrayList<>();

        for (String combo : comboList) {
            ArrayList<SubscriberInfo> subscribers = subMap.get(combo);
            if (subscribers == null) {
                System.out.println("[SERVER]: This article does not currently have any subscriptions.");
                continue;  // none have ever subscribed to this combination
            }
            byte[] message = Article.getBytes();
            for (SubscriberInfo sub : subscribers){
                if (sentToAlready.contains(sub)) {
                    continue;
                }  // don't publish this to same subscriber multiple times
                sentToAlready.add(sub);
                try{
                    // Prepare packet and send to clients
                    InetAddress address = InetAddress.getByName(sub.GetIP());
                    DatagramPacket packet = new DatagramPacket(message, message.length, address, sub.GetPort());
                    socket.send(packet);
                    System.out.printf("[SERVER]: Sent article %s to client with IP Address: %s and Port Number: %d\n", Article, sub.GetIP(), sub.GetPort());
                } catch(Exception e){
                    String errMsg =
                        "[SERVER]: Error detected while publishing to client with IP Address: %s and Port Number: %d\n";
                    System.out.printf(errMsg, sub.GetIP(), sub.GetPort());
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean ArticleValidForPublish(String article){
        // Return false if article length is greater than 60 or 120 bytes (1 char is 2 bytes in Java)
        if (article.length() > 60){
            System.out.println("[SERVER]: Article is too long. Article length cannot be more than 60 characters");
            return false;
        }

        // A correct article format has 3 semicolons, so that check should be done first
        if (article.chars().filter(ch -> ch == ';').count() != 3){
            return false;
        }
        // Return false if article format is like ";;;contents" or "contents" field is missing
        HashMap<String, String> articleMap = parseArticle(article);
        if (FirstThreeFieldsEmpty(articleMap) || articleMap.get("contents") == "") {
            return false;
        }

        return true;
    }

    private static boolean ArticleValidForSubscribeOrUnSub(String article){
        // A correct article format has 3 semicolons, so that check should be done first
        if (article.chars().filter(ch -> ch == ';').count() != 3){
            return false;
        }
        Set<String> types = new HashSet<>(Arrays.asList("Sports", "Lifestyle", "Entertainment", "Business", "Technology",
                                                        "Science", "Politics" ,"Health"));
    
        // Check if Type, Originator and Org fields are all empty. 
        HashMap<String, String> articleMap = parseArticle(article);

        // If first three fields are all empty, return False
        if (FirstThreeFieldsEmpty(articleMap)) return false;
        
        // At this point we know at least 1 of the first 3 fields is not empty
        // If the article type is present then check if it is a valid article type
        if (articleMap.get("type") != "" && !(types.contains(articleMap.get("type")))) return false;

        // Finally check if the "contents" field is empty
        if (articleMap.get("contents") != "") return false;

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
        try{
            Registry registry = LocateRegistry.getRegistry("localhost");
            PubSubServerInterface server = (PubSubServerInterface) registry.lookup("server.PubSubServer");
            System.out.println("[SERVER]: Client pinged server. Server is online.");
            return true;
        } catch (Exception e){
            return false;
        }
        // return true;
    }

    // overloaded Ping(), where the FULL server name must be passed in
    public boolean Ping(String Host, String ServerName) throws RemoteException {
        try{
            Registry registry = LocateRegistry.getRegistry(Host);
            PubSubServerInterface server = (PubSubServerInterface) registry.lookup(ServerName);
            System.out.println("[SERVER]: Client pinged server. Server is online.");
            return true;
        } catch (Exception e){
            return false;
        }
    }

    // helper returns subscription details string with no 3rd semicolon and no contents field 
    private static String unparseSubscription(HashMap<String, String> subFields) {
        return subFields.get("type") + ";"
        + subFields.get("originator") + ";"
        + subFields.get("org");
    }

    // helper function that generates all subscription strings in the format
    // topic;originator;org that are equally or less specific than the argument
    private static ArrayList<String> genLessSpecificSubs(String sub) {
        HashMap<String, String> fields = parseArticle(sub);
        ArrayList<String> comboList = new ArrayList<>();
        comboList.add(fields.get("type") + ";" + fields.get("originator")
            + ";"+ fields.get("org"));  // orignial subscription field set
        comboList.add(fields.get("type") + ";" + fields.get("originator") + ";");
        comboList.add(";" + fields.get("originator") + ";" + fields.get("org"));
        comboList.add(fields.get("type") + ";;" + fields.get("org"));
        comboList.add(fields.get("type") + ";;");
        comboList.add(";" + fields.get("originator") + ";");
        comboList.add(";;" + fields.get("org"));

        // remove all fields that generated to the trivial ";;"
        for (int i = comboList.size() - 1; i >=0; i--) {
            if (comboList.get(i).equals(";;")) {
                comboList.remove(i);
            }
        }
        return comboList;
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException
    {
        String binding = "PubSubServer";
        if (args.length > 0) {
            binding = args[0];
        }
        try{
            PubSubServerInterface ContentSrv = new PubSubServer();
            Naming.rebind("server." + binding, ContentSrv);
            System.out.println("\nPublish-Subscribe Server is ready as: " + binding);
        } catch(Exception e) {
            System.out.println("Error occurred while trying to start server. Exiting...");
            System.exit(1);
        }
    }
}
