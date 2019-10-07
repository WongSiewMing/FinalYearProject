package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import java.io.Serializable;

public class AvailableTimeOB implements Serializable {
    private String availableID;
    private String studentID;
    private String availableDate;
    private String startTime;
    private String endTime;
    private String availableStatus;
    private String recordStatus;

    public AvailableTimeOB(String availableID, String studentID, String availableDate, String startTime, String endTime, String availableStatus) {
        this.availableID = availableID;
        this.studentID = studentID;
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availableStatus = availableStatus;
    }

    public String getAvailableID() {
        return availableID;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getAvailableDate() {
        return availableDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getAvailableStatus() {
        return availableStatus;
    }

    public String getRecordStatus() {
        return recordStatus;
    }
}
