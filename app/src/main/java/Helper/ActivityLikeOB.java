package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import java.io.Serializable;

public class ActivityLikeOB implements Serializable {
    private String likeID;
    private String activityID;
    private String studentID;
    private String likestatus;

    public ActivityLikeOB(String likeID, String activityID, String studentID, String likestatus) {
        this.likeID = likeID;
        this.activityID = activityID;
        this.studentID = studentID;
        this.likestatus = likestatus;
    }

    public String getLikeID() {
        return likeID;
    }

    public String getActivityID() {
        return activityID;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getLikestatus() {
        return likestatus;
    }
}
