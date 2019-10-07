package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import java.io.Serializable;

public class ActivityCommentOB implements Serializable {
    private String commentID;
    private String activityID;
    private StudentBasicInfoOB studentID;
    private String commentText;
    private String commentTime;
    private String commentDate;

    public ActivityCommentOB(String commentID, String activityID, StudentBasicInfoOB studentID, String commentText, String commentTime, String commentDate) {
        this.commentID = commentID;
        this.activityID = activityID;
        this.studentID = studentID;
        this.commentText = commentText;
        this.commentTime = commentTime;
        this.commentDate = commentDate;
    }

    public String getCommentID() {
        return commentID;
    }

    public String getActivityID() {
        return activityID;
    }

    public StudentBasicInfoOB getStudentID() {
        return studentID;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public String getCommentDate() {
        return commentDate;
    }
}
