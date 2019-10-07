package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;

public class UserActivityOB implements Serializable {
    private String activityID;
    private StudentBasicInfoOB studentID;
    private String stuffID;
    private String activityCaption;
    private String activityThumbnail;
    private String uploadDate;
    private String uploadTime;
    private String activityStatus;

    public UserActivityOB(String activityID, StudentBasicInfoOB studentID, String stuffID, String activityCaption, String activityThumbnail, String uploadDate, String uploadTime, String activityStatus) {
        this.activityID = activityID;
        this.studentID = studentID;
        this.stuffID = stuffID;
        this.activityCaption = activityCaption;
        this.activityThumbnail = activityThumbnail;
        this.uploadDate = uploadDate;
        this.uploadTime = uploadTime;
        this.activityStatus = activityStatus;
    }

    public String getActivityID() {
        return activityID;
    }

    public StudentBasicInfoOB getStudentID() {
        return studentID;
    }

    public String getStuffID() {
        return stuffID;
    }

    public String getActivityCaption() {
        return activityCaption;
    }

    public String getActivityThumbnail() {
        return activityThumbnail;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public String getActivityStatus() {
        return activityStatus;
    }
}
