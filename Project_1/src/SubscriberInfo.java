// Subscriber class
// stores subscriber's info, which includes subscirptions

import java.util.ArrayList;

public class SubscriberInfo {
    private String _IP;
    private int _Port;

    public SubscriberInfo(String IP, int Port) {
        _IP = IP;
        _Port = Port;
    }

    public String GetIP() {
        return _IP;
    }

    public int GetPort() {
        return _Port;
    }
}