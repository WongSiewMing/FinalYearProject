package Helper;

import java.io.Serializable;

/*Author : Adelina Tang Chooi
Programme : RSD3
Year : 2017*/

public class Subscribe implements Serializable {

    private String subscribeID;
    private Student subscribeeID;
    private Student subscriberID;
    private String subscribeDate;
    private String unsubscribeDate;
    private String subscribeStatus;

    private String subscribeeID2;
    private String subscriberID2;

    public Subscribe(String subscribeID, Student subscribeeID, Student subscriberID, String subscribeDate, String unsubscribeDate, String subscribeStatus) {
        this.subscribeID = subscribeID;
        this.subscribeeID = subscribeeID;
        this.subscriberID = subscriberID;
        this.subscribeDate = subscribeDate;
        this.unsubscribeDate = unsubscribeDate;
        this.subscribeStatus = subscribeStatus;
    }

    public Subscribe(String subscribeID, String subscribeeID2, String subscriberID2, String subscribeDate, String unsubscribeDate, String subscribeStatus) {
        this.subscribeID = subscribeID;
        this.subscribeeID2 = subscribeeID2;
        this.subscriberID2 = subscriberID2;
        this.subscribeDate = subscribeDate;
        this.unsubscribeDate = unsubscribeDate;
        this.subscribeStatus = subscribeStatus;
    }

    public Student getSubscribeeID() {
        return subscribeeID;
    }

    public String getSubscribeeID2() {
        return subscribeeID2;
    }
}
