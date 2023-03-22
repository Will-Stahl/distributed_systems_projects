import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

// compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunTestClass.java 
// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass
public class RunTestClass {
    private void startServers(String consistency) throws IOException {
        procs = new ArrayList<Process>();
        for (int i = 4; i >= 0; i--) {  // start corrdinator first
            String port = Integer.toString(i + 2000);
            Process p = new ProcessBuilder("java", "-cp",
                    "BulletinBoardServer", "localhost", port, consistency).start();
            if (!p.isAlive()) {
                killSevers();
                System.out.println("Failed to start server. Aborting.");
                return;
            }
            procs.add(p);
        }
        return;
    }

    private void killSevers() {
        for (Process p : procs) {
            p.destroy();
        }
    }
    
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(BulletinBoardTest.class);

        for (Failure failure : result.getFailures()) {
           System.out.println(failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tests successful.");
        } else {
            System.out.println("Tests failed.");
        }
    }
} 