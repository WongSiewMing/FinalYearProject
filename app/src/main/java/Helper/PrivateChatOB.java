package Helper;

import java.io.Serializable;

public class PrivateChatOB implements Serializable {
    private String priChatID;
    private String studentID;
    private String studentName;
    private String recipient;
    private String message;
    private String image;
    private String postDate;
    private String postTime;
    private Student recipient2;

    public PrivateChatOB(String priChatID, String studentID, String studentName, String recipient, String message, String image, String postDate, String postTime) {
        this.priChatID = priChatID;
        this.studentID = studentID;
        this.studentName = studentName;
        this.recipient = recipient;
        this.message = message;
        this.image = image;
        this.postDate = postDate;
        this.postTime = postTime;
    }

    public PrivateChatOB(String message, String image, String postDate, String postTime, Student recipient2) {
        this.message = message;
        this.image = image;
        this.postDate = postDate;
        this.postTime = postTime;
        this.recipient2 = recipient2;
    }

    public String getPriChatID() {
        return priChatID;
    }

    public Student getRecipient2() {
        return recipient2;
    }

    public void setRecipient2(Student recipient2) {
        this.recipient2 = recipient2;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public String getImage(){
        return image;
    }

    public String getPostDate() {
        return postDate;
    }

    public String getPostTime() {
        return postTime;
    }
}
