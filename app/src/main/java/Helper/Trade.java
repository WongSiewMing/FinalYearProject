package Helper;

import java.io.Serializable;

/*Author : Wong Siew Ming
Programme : RSD3
Year : 2019*/

public class Trade implements Serializable {
    private String tradeID;
    private Stuff userStuffID;
    private Stuff requestStuffID;
    private Student studentID;
    private Student sellerID;
    private String tradeStatus;
    private String tradeDate;
    private String tradeTime;

    public Trade(String tradeID, Stuff userStuffID, Stuff requestStuffID, Student studentID, Student sellerID, String tradeStatus, String tradeDate, String tradeTime) {
        this.tradeID = tradeID;
        this.userStuffID = userStuffID;
        this.requestStuffID = requestStuffID;
        this.studentID = studentID;
        this.sellerID = sellerID;
        this.tradeStatus = tradeStatus;
        this.tradeDate = tradeDate;
        this.tradeTime = tradeTime;
    }

    public String getTradeID() {
        return tradeID;
    }

    public Stuff getUserStuffID() {
        return userStuffID;
    }

    public Stuff getRequestStuffID() {
        return requestStuffID;
    }

    public Student getStudentID() {
        return studentID;
    }

    public Student getSellerID() {
        return sellerID;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public String getTradeTime() {
        return tradeTime;
    }
}
