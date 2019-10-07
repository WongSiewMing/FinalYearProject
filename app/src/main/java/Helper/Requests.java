package Helper;

import java.io.Serializable;


public class Requests implements Serializable {
    private String requeststuffID;
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

    public Requests(String requeststuffID, Student studentID, String stuffName, String stuffImage, String stuffDescription,
                    String stuffCategory, String stuffCondition, double stuffPrice, int stuffQuantity,
                    String validStartDate, String validEndDate, String stuffStatus) {
        this.requeststuffID = requeststuffID;
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


    public String getRequeststuffID() {
        return requeststuffID;
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

}
