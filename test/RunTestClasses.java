import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

// compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunTestClasses.java 
// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClasses 
public class RunTestClasses {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(PrivatePubSubMethodsTest.class);

        for (Failure failure : result.getFailures()) {
           System.out.println(failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tests for private methods successful.");
        } else {
            System.out.println("Tests for private methods failed.");
        }

        // run test for public methods
        result = JUnitCore.runClasses(PublicPubSubMethodsTest.class);

        for (Failure failure : result.getFailures()) {
           System.out.println(failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tests for public methods successful.");
        } else {
            System.out.println("Tests for public methods failed.");
        }
    }
} 