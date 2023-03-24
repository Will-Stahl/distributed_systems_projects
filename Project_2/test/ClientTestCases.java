import org.junit.*;
import java.util.*;


public class ClientTestCases {
    private boolean ValidPostRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.contains(":")){
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            return false;
        }

        String articleString = parts[1].trim();
        return ValidArticleFormat(articleString);
    }

    private boolean ValidReplyRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.contains(":")){
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            return false;
        }

        String replyString = parts[1].trim();
        if (!replyString.contains(";")){
            return false;
        }

        String[] replyParts = replyString.split(";");
        if (replyParts.length != 3){
            return false;
        }

        String articleNumber = replyParts[0].trim();
        if (!articleNumber.matches("\\d+")){
            return false;
        }

        return ValidArticleFormat(replyParts[1].trim() + ";" + replyParts[2].trim());
    }

    private boolean ValidArticleFormat(String articleString){
        if (!articleString.contains(";")){
            return false;
        }

        String[] articleParts = articleString.split(";");
        if (articleParts.length != 2){
            return false;
        }

        if (articleParts[0].trim().length() == 0){
            return false;
        }

        if (articleParts[1].trim().length() == 0){
            return false;
        }

        return true;
    }

    private boolean ValidChooseRequest(String lowerCaseRequest) {
        if (!lowerCaseRequest.contains(":")){
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            return false;
        }

        String articleNumber = parts[1].trim();
        if (!articleNumber.matches("\\d+")){
            return false;
        }

        return true;
    }

    private boolean ValidJoinOrLeaveRequestFormat(String lowerCaseRequest){
        if (!lowerCaseRequest.contains(":")){
            return false;
        }

        String[] parts = lowerCaseRequest.split(":");
        if (parts.length != 2){
            return false;
        }

        try{
            int serverPort = Integer.parseInt(parts[1].trim());
            // If port is invalid, then print error message and exit.
            if (!CheckValidPort(serverPort)){
                return false;
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private static boolean CheckValidPort(int port){
        Set<Integer> ports = new HashSet<>();
        ports.add(2000);
        ports.add(2001);
        ports.add(2002);
        ports.add(2003);
        ports.add(2004);

        return ports.contains(port);
    }

    @Test
    public void CheckValidPostRequest(){
        String post = "post: movies;inception is great!";
        Assert.assertEquals(ValidPostRequest(post), true);

        post = "   post :        movies;inception is great!     ";
        Assert.assertEquals(ValidPostRequest(post), true);

        post = "   post :        movies      ;          inception is great!";
        Assert.assertEquals(ValidPostRequest(post), true);
    }

    @Test
    public void CheckInvalidPostRequest(){
        String post = "post movies;inception is great!";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "post : moviesinception is great!";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "   post :        ;  inception is great!";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "   post :        movies;";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "   post :        ;";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "post:;";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "post:";
        Assert.assertEquals(ValidPostRequest(post), false);

        post = "post";
        Assert.assertEquals(ValidPostRequest(post), false);
    }

    @Test
    public void CheckValidReplyRequest(){
        String reply = "reply: 1;movies;interstellar is great!";
        Assert.assertEquals(ValidReplyRequest(reply), true);

        reply = "   reply :  1;      movies;interstellar is great!        ";
        Assert.assertEquals(ValidReplyRequest(reply), true);

        reply = "   reply :   1;     movies      ;          interstellar is great!";
        Assert.assertEquals(ValidReplyRequest(reply), true);

        reply = "   reply :   1;     movies ;!";
        Assert.assertEquals(ValidReplyRequest(reply), true);
    }

    @Test
    public void CheckInvalidReplyRequest(){
        
        String reply = "reply";
        Assert.assertEquals(ValidReplyRequest(reply), false);

        reply = "reply:";
        Assert.assertEquals(ValidReplyRequest(reply), false);

        reply = "reply:     a;  movies  ;   interstellar is great!  ";
        Assert.assertEquals(ValidReplyRequest(reply), false);

        reply = "reply:     ;  movies  ;   interstellar is great!  ";
        Assert.assertEquals(ValidReplyRequest(reply), false);

        reply = "reply:     ;  ;   interstellar is great!  ";
        Assert.assertEquals(ValidReplyRequest(reply), false);

        reply = "reply:     ;  ;  ";
        Assert.assertEquals(ValidReplyRequest(reply), false);
    }

    @Test
    public void CheckValidChooseRequest(){
        String choose = "choose: 1";
        Assert.assertEquals(ValidChooseRequest(choose), true);

        choose = "  choose  : 1  ";
        Assert.assertEquals(ValidChooseRequest(choose), true);
    }

    @Test
    public void CheckInvalidChooseRequest(){
        String choose = "choose: ";
        Assert.assertEquals(ValidChooseRequest(choose), false);

        choose = "choose 1";
        Assert.assertEquals(ValidChooseRequest(choose), false);

        choose = "choose: abc";
        Assert.assertEquals(ValidChooseRequest(choose), false);
    }

    @Test
    public void CheckValidJoinOrLeaveRequest(){
        String choose = "join: 2000";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), true);

        choose = "  join  : 2001  ";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), true);

        choose = "leave: 2002";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), true);

        choose = "  leave  : 2003  ";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), true);
    }

    @Test
    public void CheckInvalidJoinOrLeaveRequest(){
        String choose = "join";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), false);

        choose = "  join :  ";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), false);

        choose = "join: 2005";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), false);
        
        choose = "leave";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), false);

        choose = "  leave :  ";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), false);

        choose = "  leave :  2008";
        Assert.assertEquals(ValidJoinOrLeaveRequestFormat(choose), false);
    }
}
