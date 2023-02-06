package test;

import org.junit.*;
import java.util.*;

public class PubSubPrivateMethodsTest {
    private boolean IsValidIPAddress(String IP) {
        String[] parts = IP.split("\\.");

        if (parts.length != 4) return false;

        for (int i = 0; i < parts.length; i++){
            String part = parts[i];
            if (Integer.parseInt(part) < 0 || Integer.parseInt(part) > 255) return false;
        }
        return true;
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

    // Check if valid IP addresses return True
    @Test
    public void CheckValidIP(){
        String IP = "1.2.3.4";
        Assert.assertTrue(IsValidIPAddress(IP));

        IP = "0.196.3.255";
        Assert.assertTrue(IsValidIPAddress(IP));
    }

    // Check if invalid IP addresses return False
    @Test
    public void CheckInValidIP(){
        String IP = "0.196.3.257";
        Assert.assertFalse(IsValidIPAddress(IP));

        IP = "1.2.3";
        Assert.assertFalse(IsValidIPAddress(IP));

        IP = "0.0.0.256";
        Assert.assertFalse(IsValidIPAddress(IP));
    }

    // Check if valid article formats return True for "Publish"
    @Test
    public void CheckValidArticleForPublish(){
        String articleName = "Sports;;;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));

        articleName = ";Someone;UMN;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));
        
        articleName = "Science;Someone;UMN;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));

        articleName = "Science;Someone;UMN;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));

        articleName = "  ;Someone;UMN;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));

        articleName = ";;UMN;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));

        articleName = "  ;Someone;;contents      ";
        Assert.assertTrue(ArticleValidForPublish(articleName));

        articleName = "NotSports; Someone;   UMN;contents";
        Assert.assertTrue(ArticleValidForPublish(articleName));
    }

    // Check if invalid article formats return False for "Publish"
    @Test
    public void CheckInValidArticleForPublish(){
        String articleName = "Sports;;;";
        Assert.assertFalse(ArticleValidForPublish(articleName));

        articleName = ";Someone;UMN;";
        Assert.assertFalse(ArticleValidForPublish(articleName));
        
        articleName = ";;;contents";
        Assert.assertFalse(ArticleValidForPublish(articleName));

        articleName = "Science;UMN;contents";
        Assert.assertFalse(ArticleValidForPublish(articleName));

        articleName = ";;;;";
        Assert.assertFalse(ArticleValidForPublish(articleName));

        articleName = "     ";
        Assert.assertFalse(ArticleValidForPublish(articleName));
    }

    // Check if valid article formats return True for "Subscribe"
    @Test
    public void CheckValidArticleForSubscribe(){
        String articleName = "Sports;;;";
        Assert.assertTrue(ArticleValidForSubscribe(articleName));

        articleName = ";Someone;UMN;";
        Assert.assertTrue(ArticleValidForSubscribe(articleName));
        
        articleName = "Science;;UMN;";
        Assert.assertTrue(ArticleValidForSubscribe(articleName));

        articleName = ";;UMN;";
        Assert.assertTrue(ArticleValidForSubscribe(articleName));
    }

    // Check if invalid article formats return False for "Subscribe"
    @Test
    public void CheckInValidArticleForSubscribe(){
        String articleName = ";;;contents";
        Assert.assertFalse(ArticleValidForSubscribe(articleName));

        articleName = ";;UMN;contents";
        Assert.assertFalse(ArticleValidForSubscribe(articleName));

        articleName = ";;;";
        Assert.assertFalse(ArticleValidForSubscribe(articleName));

        articleName = " ";
        Assert.assertFalse(ArticleValidForSubscribe(articleName));
    }
}
