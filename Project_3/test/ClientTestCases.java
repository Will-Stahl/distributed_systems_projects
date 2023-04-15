import org.junit.*;
import java.util.*;


public class ClientTestCases {
    private static boolean ValidateFindAndDownloadRequest(String request){
        // If "Find" format is invalid, then print an error and return
        String[] parts = request.split(":");
        if (parts.length != 2){
            return false;
        }

        // Example: If rquest format is "Find: foo.text", then parts[2] = "foot.txt"
        // Remove any leading or trailing whitespaces from the second part of the request string
        parts[1] = parts[1].trim();

        // Example: If second part of request string has the form "foo.text hello.txt" or "foo   .txt", then print error and return
        if (parts[1].contains(" ")){
            return false;
        }

        // Example: If second part of request string has the form "foo", then print error and return
        if (!parts[1].contains(".")){
            return false;
        }

        return true;
    }

    @Test
    public void CheckValidFindAndDownloadRequests(){
        String request = "find: foo.txt";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), true);

        request = "     find     :  foo.txt     ";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), true);

        request = "download: foo.txt";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), true);

        request = "     download     :  foo.txt     ";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), true);
    }

    @Test
    public void CheckInValidFindAndDownloadRequests(){
        String request = "find: foo";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "find:";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "find: foo    .txt";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "find: foo.txt foo2.txt";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "download: foo";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "download:";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "download: foo    .txt";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);

        request = "download: foo.txt foo2.txt";
        Assert.assertEquals(ValidateFindAndDownloadRequest(request), false);
    }
}
