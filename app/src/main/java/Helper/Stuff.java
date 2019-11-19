package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Stuff implements Serializable {
    private String stuffID;
    private Student studentID;
    private String stuffName;
    private String stuffImage;
    private String stuffDescription;
    private String stuffCategory;
    private String stuffCondition;
    private double stuffPrice;
    private int stuffQuantity;
    private String validStartDate;
    private String validEndDate;
    private String stuffStatus;
    private String stuffStudentID;

    public Stuff(String stuffID, Student studentID, String stuffName, String stuffImage, String stuffDescription,
                 String stuffCategory, String stuffCondition, double stuffPrice, int stuffQuantity,
                 String validStartDate, String validEndDate, String stuffStatus) {
        this.stuffID = stuffID;
        this.studentID = studentID;
        this.stuffName = stuffName;
        this.stuffImage = stuffImage;
        this.stuffDescription = stuffDescription;
        this.stuffCategory = stuffCategory;
        this.stuffCondition = stuffCondition;
        this.stuffPrice = stuffPrice;
        this.stuffQuantity = stuffQuantity;
        this.validStartDate = validStartDate;
        this.validEndDate = validEndDate;
        this.stuffStatus = stuffStatus;
    }

    public Stuff(String stuffID, String stuffName, String stuffImage, String stuffDescription, String stuffStudentID) {
        this.stuffID = stuffID;
        this.stuffName = stuffName;
        this.stuffImage = stuffImage;
        this.stuffDescription = stuffDescription;
        this.stuffStudentID = stuffStudentID;
    }

    public Stuff(String stuffID, Student studentID, String stuffName, String stuffImage, double stuffPrice, int stuffQuantity, String stuffStatus) {
        this.stuffID = stuffID;
        this.studentID = studentID;
        this.stuffName = stuffName;
        this.stuffImage = stuffImage;
        this.stuffPrice = stuffPrice;
        this.stuffQuantity = stuffQuantity;
        this.stuffStatus = stuffStatus;
    }

    public Stuff(String stuffName, String stuffImage, double stuffPrice) {
        this.stuffName = stuffName;
        this.stuffImage = stuffImage;
        this.stuffPrice = stuffPrice;
    }

    public Stuff(Student studentID, String stuffID, String stuffName, String stuffImage, double stuffPrice, String stuffStatus) {
        this.studentID = studentID;
        this.stuffID = stuffID;
        this.stuffName = stuffName;
        this.stuffImage = stuffImage;
        this.stuffPrice = stuffPrice;
        this.stuffStatus = stuffStatus;
    }

    public String getStuffID() {
        return stuffID;
    }

    public Student getStudentID() {
        return studentID;
    }

    public String getStuffName() {
        return stuffName;
    }

    public String getStuffImage() {
        return stuffImage;
    }

    public String getStuffDescription() {
        return stuffDescription;
    }

    public String getStuffCategory() {
        return stuffCategory;
    }

    public String getStuffCondition() {
        return stuffCondition;
    }

    public double getStuffPrice() {
        return stuffPrice;
    }

    public int getStuffQuantity() {
        return stuffQuantity;
    }

    public String getValidStartDate() {
        return validStartDate;
    }

    public String getValidEndDate() {
        return validEndDate;
    }

    public String getStuffStatus() {
        return stuffStatus;
    }
}
