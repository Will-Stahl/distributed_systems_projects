import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunClientTestCases
public class RunClientTestCases {
    public static void main(String[] args){
        Result result = JUnitCore.runClasses(ClientTestCases.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tests for private methods successful.");
        } else {
            System.out.println("Tests for private methods failed.");
        }
    }
}
