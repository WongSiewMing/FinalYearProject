package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;

public class StoreCommentOB implements Serializable {
    private String storeComtID;
    private String storeID;
    private String sComment;
    private String commentDate;
    private String commentTime;

    public StoreCommentOB(String storeComtID, String storeID, String sComment, String commentDate, String commentTime) {
        this.storeComtID = storeComtID;
        this.storeID = storeID;
        this.sComment = sComment;
        this.commentDate = commentDate;
        this.commentTime = commentTime;
    }

    public String getStoreComtID() {
        return storeComtID;
    }

    public void setStoreComtID(String storeComtID) {
        this.storeComtID = storeComtID;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getsComment() {
        return sComment;
    }

    public void setsComment(String sComment) {
        this.sComment = sComment;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }
}
