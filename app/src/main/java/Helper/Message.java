package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Message implements Serializable {

    private String messageID;
    private String sender;
    private String roomID;
    private String message;
    private String postDate;
    private String postTime;
    private String studentName;
    private StudentBasicInfoOB studentID;

    public Message(String sender, String roomID, String message, String postDate, String postTime, String studentName) {

        this.sender = sender;
        this.roomID = roomID;
        this.message = message;
        this.postDate = postDate;
        this.postTime = postTime;
        this.studentName = studentName;
    }

    public Message(String messageID, String sender, String roomID, String message, String postDate, String postTime, StudentBasicInfoOB studentID) {
        this.messageID = messageID;
        this.sender = sender;
        this.roomID = roomID;
        this.message = message;
        this.postDate = postDate;
        this.postTime = postTime;
        this.studentID = studentID;
    }

    public String getMessage() {
        return message;
    }

    public String getPostDate() {
        return postDate;
    }

    public String getPostTime() {
        return postTime;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getSender() {
        return sender;
    }

    public String getRoomID() {
        return roomID;
    }

    public StudentBasicInfoOB getStudentID() {
        return studentID;
    }
}
