package Helper;

import java.io.Serializable;

/*Author : Wong Siew Ming
Programme : RSD3
Year : 2019*/

public class ViewHistoryOB implements Serializable {
    private String ViewHistoryID;
    private Student StudentID;
    private String date;
    private String time;
    private Stuff StuffID;
    private String status;

    public ViewHistoryOB(String viewHistoryID, Student studentID, String date, String time, Stuff stuffID, String status) {
        this.ViewHistoryID = viewHistoryID;
        this.StudentID = studentID;
        this.date = date;
        this.time = time;
        this.StuffID = stuffID;
        this.status = status;
    }

    public ViewHistoryOB(String viewHistoryID) {
        this.ViewHistoryID = viewHistoryID;
    }

    public String getViewHistoryID() {
        return ViewHistoryID;
    }

    public Student getStudentID() {
        return StudentID;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public Stuff getStuffID() {
        return StuffID;
    }

    public String getStatus() {
        return status;
    }
}
