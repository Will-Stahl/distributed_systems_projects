import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.*;

public class PubSubServer extends UnicastRemoteObject
implements PubSubServerInterface
{
    
    private ArrayList<SubscriberInfo> Subscribers;
    private HashMap<String, ArrayList<SubscriberInfo>> articles;
    private final static int PORT_NUMBER = 1099;
    private static DatagramSocket socket;
    private final int MAX_CLIENTS = 10;
    private static int clientCount = 0;

    public PubSubServer() throws RemoteException, IOException
    {
        Subscribers = new ArrayList<SubscriberInfo>();
        articles = new HashMap<>();
        socket = new DatagramSocket(PORT_NUMBER);
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
        if (clientCount > MAX_CLIENTS) {
            System.out.println("Server Capacity reached! Please try joining again later");
            return false;
        }

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

        // check for valid IP address
        if (IsValidIPAddress(IP)){
            Subscribers.add(new SubscriberInfo(IP, Port));
            System.out.printf("Added new client with IP: %s, Port: %d\n", IP, Port);
            return true;
        }
        System.out.println("Invalid IP Address");
        return false;
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
            System.out.print("Client was not already joined\n");
            return false;
        }
        // TODO: test removing client from all articles as well
        final SubscriberInfo fnlSubPtr = subPtr;
        articles.forEach((k, v) -> {
            if (v.contains(fnlSubPtr)) {
                v.remove(fnlSubPtr);
            }});
        
        System.out.print("Removed subscriber\n");
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
        if (ArticleValidForSubscribeOrUnSub(Article)){
            // If article has not been created earlier, then we should create it
            if (!articles.containsKey(Article)){
                articles.put(Article, new ArrayList<SubscriberInfo>());
            }

            // Add current client to the article subscriber list
            for (SubscriberInfo sub : Subscribers){
                if (sub.GetIP() == IP && sub.GetPort() == Port){
                    if (articles.get(Article).contains(sub)) {
                        // TODO: may not need to return false here, but don't duplicate subscriptions
                        return false;
                    }
                    articles.get(Article).add(sub);
                    System.out.printf("Client with IP Address %s has subscribed to Article %s.",IP, Article);
                    return true;
                }
            }
        } 
        System.out.println("Article type not valid for subscribing.");
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
        if (ArticleValidForSubscribeOrUnSub(Article)){
            // If the article hasn't been published earlier, then return false
            if (!articles.containsKey(Article)){
                System.out.println("Article does not exist and cannot be unsubscribed from");
                return false;
            }

            // Get all subscribers to the current article and remove the client
            ArrayList<SubscriberInfo> subscribers = articles.get(Article);
            for(int i = 0; i < subscribers.size(); i++){
                SubscriberInfo sub = subscribers.get(i);
                if (sub.GetIP() == IP && sub.GetPort() == Port){
                    System.out.printf("Client with IP Address %s has unsubscribed from Article %s.",IP, Article);
                    subscribers.remove(i);
                    return true;
                }
            }
        }
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
        if (ArticleValidForPublish(Article)){
            // If the article has already been published, then don't publish it again
            if (articles.containsKey(Article)) {
                System.out.println("Article has already been published earlier.");
                return false;
            }

            // TODO: Clients with less specific subscriptions need to receive
            //          more specific articles that match the non-blank fields
            //          in the subscription!

            // Get current list of clients subscribed to an article
            ArrayList<SubscriberInfo> subscribers = articles.get(Article);
            byte[] message = Article.getBytes();
            for (SubscriberInfo sub : subscribers){
                try{
                    // Prepare packet and send to clients
                    InetAddress address = InetAddress.getByName(sub.GetIP());
                    DatagramPacket packet = new DatagramPacket(message, message.length, address, sub.GetPort());
                    socket.send(packet);
                } catch(Exception e){
                    System.out.printf("Error detected while publishing to client with IP Address: %s and Port Number: %d", sub.GetIP(), sub.GetPort());
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean ArticleValidForPublish(String article){
        // Return false if article format is like ";;;contents" or "contents" field is missing
        HashMap<String, String> articleMap = parseArticle(article);
        if (FirstThreeFieldsEmpty(articleMap) || articleMap.get("contents") == "") return false;

        return true;
    }

    private static boolean ArticleValidForSubscribeOrUnSub(String article){
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
        return true;
    }

    private static void HandleClientRequest(String clientRequest, String address, PubSubServerInterface ContentSrv) throws RemoteException{
        try{
            while(true){
                if (clientRequest.startsWith("join")){
                    clientCount += 1;
                    ContentSrv.Join(address, PORT_NUMBER);
                } else if (clientRequest.startsWith("leave") && clientCount > 0){
                    clientCount -= 1;
                    ContentSrv.Leave(address, PORT_NUMBER);
                } else if (clientRequest.startsWith("publish:")){
                    String[] words = clientRequest.split(":");
                    ContentSrv.Publish(words[1].trim(), address, PORT_NUMBER);
                } else if (clientRequest.startsWith("subscribe:")){
                    String[] words = clientRequest.split(":");
                    ContentSrv.Subscribe(address, PORT_NUMBER, words[2].trim());
                } else if (clientRequest.startsWith("unsubscribe:")){
                    String[] words = clientRequest.split(":");
                    ContentSrv.Unsubscribe(address, PORT_NUMBER, words[2].trim());
                } else if (clientRequest.startsWith("ping")){
                    ContentSrv.Ping();
                }
            }
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

    private static void ProcessRequestFromClient(PubSubServerInterface ContentSrv) throws IOException{
        try{
            byte[] request = new byte[1024];
            DatagramPacket packet = new DatagramPacket(request, request.length);
            socket.receive(packet);
            String address = packet.getAddress().getHostAddress();
            String clientRequest = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Request from client: " + clientRequest);
            HandleClientRequest(clientRequest, address, ContentSrv);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws RemoteException, MalformedURLException
    {
        try{
            LocateRegistry.createRegistry(PORT_NUMBER);
            PubSubServerInterface ContentSrv = new PubSubServer();
            Naming.rebind("server.PubSubServer", ContentSrv);
            System.out.println("Publish-Subscribe Server is ready.");
            ProcessRequestFromClient(ContentSrv);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}