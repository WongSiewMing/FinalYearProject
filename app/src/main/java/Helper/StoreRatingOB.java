package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;

public class StoreRatingOB implements Serializable{
    private String storeRatID;
    private String storeID;
    private String rateDate;
    private String rateTime;
    private String rateValue;
    private String comments;
    private String studentID;
    private String studentName;
    private String studentImage;

    public StoreRatingOB(String storeRatID, String storeID, String rateDate, String rateTime, String rateValue, String comments, String studentID, String studentName, String studentImage) {
        this.storeRatID = storeRatID;
        this.storeID = storeID;
        this.rateDate = rateDate;
        this.rateTime = rateTime;
        this.rateValue = rateValue;
        this.comments = comments;
        this.studentID = studentID;
        this.studentName = studentName;
        this.studentImage = studentImage;
    }

    public String getStoreRatID() {
        return storeRatID;
    }

    public String getStoreID() {
        return storeID;
    }

    public String getRateDate() {
        return rateDate;
    }

    public String getRateTime() {
        return rateTime;
    }

    public String getRateValue() {
        return rateValue;
    }

    public String getComments() {
        return comments;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentImage() {
        return studentImage;
    }
}
