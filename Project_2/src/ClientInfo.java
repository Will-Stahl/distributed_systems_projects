public class ClientInfo {
    private String IP;
    private int Port;

    public ClientInfo(String IP, int Port){
        this.IP = IP;
        this.Port = Port;
    }

    public String GetIP(){
        return IP;
    }

    public int GetPort(){
        return Port;
    }
}
