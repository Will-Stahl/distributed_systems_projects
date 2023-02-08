import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);

        System.out.println("- pick from options 1 to 6 -");
        options();
        boolean loop = true;

        while(loop){
            int command = 0;

            
            while(true){
                try{
                    command = sc.nextInt();
                    break;
                } catch (InputMismatchException exception){
                    System.out.println("non int entered");
                }
                sc.nextLine();
            }
            
            
            
            switch (command)
            {
                case 1:
                    System.out.println("run join");
                    break;
                case 2:
                    System.out.println("run leave");
                    break;
                case 3:
                    System.out.println("run subscribe");
                    break;
                case 4:
                    System.out.println("run unsubscribe");
                    break;
                case 5:
                    System.out.println("run publish");
                    break;
                case 6:
                    System.out.println("run ping");
                    break;
                case 7:
                    System.out.println("exit");
                    loop = false;
                    break;
                default:
                    System.out.println("wrong command");
                    break;
            }
        
        }
        sc.close();
        
    }

    public static void options(){
        System.out.println("1. Join (IP, Port)");
        System.out.println("2. Leave (IP, Port)");
        System.out.println("3. Subscribe (IP, Port, Article)");
        System.out.println("4. Unsubscribe (IP, Port, Article)");
        System.out.println("5. Publish (Article, IP, Port)");
        System.out.println("6. Ping()");
        System.out.println("7. exit");
    }
}
