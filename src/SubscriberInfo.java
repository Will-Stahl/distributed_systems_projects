// Subscriber class
// stores subscriber's info, which includes subscirptions

import java.util.ArrayList;

public class SubscriberInfo {
    private String _IP;
    private int _Port;
    private ArrayList<String> Subscriptions;

    public SubscriberInfo(String IP, int Port) {
        _IP = IP;
        _Port = Port;
        Subscriptions = new ArrayList<String>();
    }

    public String GetIP() {
        return _IP;
    }

    public int GetPort() {
        return _Port;
    }
}