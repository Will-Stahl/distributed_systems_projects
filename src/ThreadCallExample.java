public class ThreadCallExample {
    public static void main(String args[]) {
        
        // three threads example
        for(int i=0; i<3; i++){
            ThreadExample thread = new ThreadExample();
            thread.start();
        }
        
    }
}
