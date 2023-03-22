import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

// compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunTestClass.java 
// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass <sequential|quorum|readyourwrites>
public class RunTestClass {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java RunTestClass <sequential|quorum|readyourwrites>");
            System.exit(0);
        }
        Result result = null;
        if (args[0].equals("sequential")) {
            result = JUnitCore.runClasses(SequentialTest.class);
        } else if (args[0].equals("quorum")) {
            result = JUnitCore.runClasses(QuorumTest.class);
        } else if (args[0].equals("readyourwrites")) {
            result = JUnitCore.runClasses(ReadYourWritesTest.class);
        } else {
            System.out.println("Usage: java RunTestClass <sequential|quorum|readyourwrites>");
            System.exit(0);
        }

        for (Failure failure : result.getFailures()) {
           System.out.println(failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tests successful for " + args[0]);
        } else {
            System.out.println("Tests failed for " + args[0]);
        }
    }
    
} 