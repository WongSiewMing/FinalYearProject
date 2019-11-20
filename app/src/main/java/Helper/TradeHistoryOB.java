package Helper;

import java.io.Serializable;

import Helper.Student;

public class TradeHistoryOB implements Serializable {
    private String TradeHistoryID;
    private Student studentID;
    private String date;
    private String time;
    private Trade TradeID;
    private String status;

    public TradeHistoryOB(String tradeHistoryID, Student studentID, String date, String time, Trade tradeID, String status) {
        this.TradeHistoryID = tradeHistoryID;
        this.studentID = studentID;
        this.date = date;
        this.time = time;
        this.TradeID = tradeID;
        this.status = status;
    }


    public String getTradeHistoryID() {
        return TradeHistoryID;
    }

    public Student getStudentID() {
        return studentID;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public Trade getTradeID() {
        return TradeID;
    }

    public String getStatus() {
        return status;
    }
}
