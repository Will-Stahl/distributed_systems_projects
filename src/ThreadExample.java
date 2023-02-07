public class ThreadExample extends Thread {
    
    @Override
    public void run(){
        String[] arr = new String[] {"a","b","c"};
        for(int i=0; i<arr.length; i++){

            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("error");
            }

            System.out.println(arr[i]);
        }

    }
}
