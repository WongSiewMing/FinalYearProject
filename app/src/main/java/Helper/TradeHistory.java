package Helper;

import java.io.Serializable;

import Helper.Student;

public class TradeHistory implements Serializable {
    private String tradeID;
    private Student studentID;
    private String customerID;
    private String stuffID;
    private String tradeDate;
    private String tradeTime;

    public TradeHistory(String tradeID, Student studentID, String customerID, String stuffID, String tradeDate, String tradeTime) {
        this.tradeID = tradeID;
        this.studentID = studentID;
        this.customerID = customerID;
        this.stuffID = stuffID;
        this.tradeDate = tradeDate;
        this.tradeTime = tradeTime;
    }

    public String getTradeID() {
        return tradeID;
    }

    public void setTradeID(String tradeID) {
        this.tradeID = tradeID;
    }

    public Student getStudentID() {
        return studentID;
    }

    public void setStudentID(Student studentID) {
        this.studentID = studentID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getStuffID() {
        return stuffID;
    }

    public void setStuffID(String stuffID) {
        this.stuffID = stuffID;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(String tradeTime) {
        this.tradeTime = tradeTime;
    }
}
