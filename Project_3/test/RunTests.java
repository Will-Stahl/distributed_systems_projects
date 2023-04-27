import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

// compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunTests.java 
// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTests
public class RunTests {
    public static void main(String[] args){
        Result result = JUnitCore.runClasses(SystemTests.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tests for RMI calls successful.");
        } else {
            System.out.println("Tests for RMI calls failed.");
        }
        // ensure processes aren't left over from failure
        ProcessHandle ph = ProcessHandle.current();
        ph.children().forEach(child -> child.destroy());
        // ProcessHandle[] children = (ProcessHandle[]) ph.children().toArray();
        // for (int i = 0; i < children.length; i++) {
        //     children[i].destroy();
        // }
    }
}