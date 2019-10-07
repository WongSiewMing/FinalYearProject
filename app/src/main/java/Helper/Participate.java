package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Participate implements Serializable {

    private String participateID;
    private Student participant;
    private Room roomID;
    private String status;

    public Participate(String participateID, Student participant, Room roomID, String status) {

        this.participateID = participateID;
        this.participant = participant;
        this.roomID = roomID;
        this.status = status;
    }

    public String getParticipateID() {

        return participateID;
    }

    public Student getParticipant() {

        return participant;
    }

    public Room getRoomID() {

        return roomID;
    }

    public String getStatus() {

        return status;
    }

}
