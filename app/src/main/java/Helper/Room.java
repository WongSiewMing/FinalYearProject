package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Room implements Serializable {

    private String roomID;
    private String creator;
    private String subject;
    private String photo;
    private String createDate;
    private String createTime;
    private String status;
    private String checkParticipant;

    public Room(String roomID, String creator, String subject, String photo, String createDate, String createTime, String status, String checkParticipant){
        this.roomID = roomID;
        this.creator = creator;
        this.subject = subject;
        this.photo = photo;
        this.createDate = createDate;
        this.createTime = createTime;
        this.status = status;
        this.checkParticipant = checkParticipant;

    }

    public String getRoomID() {
        return roomID;
    }

    public String getSubject() {
        return subject;
    }

    public String getPhoto() {
        return photo;
    }

    public String getCheckParticipant() {
        return checkParticipant;
    }
}
