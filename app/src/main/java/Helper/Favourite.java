package Helper;

import java.io.Serializable;

/*Author : Adelina Tang Chooi
Programme : RSD3
Year : 2017*/

public class Favourite implements Serializable {

    private String favouriteID;
    private Stuff stuffID;
    private Student studentID;
    private String favouriteDate;
    private String unfavouriteDate;
    private String favouriteStatus;

    public Favourite(String favouriteID, Stuff stuffID, Student studentID, String favouriteDate, String unfavouriteDate, String favouriteStatus) {
        this.favouriteID = favouriteID;
        this.stuffID = stuffID;
        this.studentID = studentID;
        this.favouriteDate = favouriteDate;
        this.unfavouriteDate = unfavouriteDate;
        this.favouriteStatus = favouriteStatus;
    }

    public Stuff getStuffID() {
        return stuffID;
    }

    public Student getStudentID() {
        return studentID;
    }

}
