package Helper;

import java.io.Serializable;

/*Author : Adelina Tang Chooi
Programme : RSD3
Year : 2017*/

public class PrivateChat implements Serializable {

    private String studentID;
    private String studentName;
    private String recipient;
    private String message;
    private String image;
    private String postDate;
    private String postTime;
    private Student recipient2;

    public PrivateChat(String studentID, String studentName, String recipient, String message, String image, String postDate, String postTime) {

        this.studentID = studentID;
        this.studentName = studentName;
        this.recipient = recipient;
        this.message = message;
        this.image = image;
        this.postDate = postDate;
        this.postTime = postTime;
    }

    public PrivateChat(Student recipient2, String message, String image, String postDate, String postTime){

        this.recipient2 = recipient2;
        this.message = message;
        this.image = image;
        this.postDate = postDate;
        this.postTime = postTime;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
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

    public Student getRecipient2() {
        return recipient2;
    }
}
