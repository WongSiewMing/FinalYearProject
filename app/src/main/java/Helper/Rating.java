package Helper;

import java.io.Serializable;

/*Author : Adelina Tang Chooi
Programme : RSD3
Year : 2017*/

public class Rating implements Serializable {

    private String ratingID;
    private Student ratingReceiver;
    private Student ratingGiver;
    private String ratingValue;
    private String experience;
    private String comments;
    private String ratingDate;
    private String ratingTime;

    public Rating(String ratingID, Student ratingReceiver, Student ratingGiver, String ratingValue, String experience, String comments, String ratingDate, String ratingTime) {
        this.ratingID = ratingID;
        this.ratingReceiver = ratingReceiver;
        this.ratingGiver = ratingGiver;
        this.ratingValue = ratingValue;
        this.experience = experience;
        this.comments = comments;
        this.ratingDate = ratingDate;
        this.ratingTime = ratingTime;
    }

    public Student getRatingGiver() {
        return ratingGiver;
    }

    public String getRatingValue() {
        return ratingValue;
    }

    public String getExperience() {
        return experience;
    }

    public String getComments() {
        return comments;
    }

    public String getRatingDate() {
        return ratingDate;
    }

    public String getRatingTime() {
        return ratingTime;
    }
}
