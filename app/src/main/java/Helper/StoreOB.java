package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;
import java.sql.Time;

import Helper.Student;

public class StoreOB implements Serializable{
    private String storeID;
    private String studentID;
    private String storeName;
    private String storeImage;
    private String storeDescription;
    private String storeCategory;
    private String openTime;
    private String closeTime;
    private String storeStatus;
    private String storeLocation;

    public StoreOB(String storeID, String studentID, String storeName, String storeImage, String storeDescription, String storeCategory, String openTime, String closeTime, String storeStatus, String storeLocation) {
        this.storeID = storeID;
        this.studentID = studentID;
        this.storeName = storeName;
        this.storeImage = storeImage;
        this.storeDescription = storeDescription;
        this.storeCategory = storeCategory;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.storeStatus = storeStatus;
        this.storeLocation = storeLocation;
    }

    public String getStoreID() {
        return storeID;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreImage() {
        return storeImage;
    }

    public String getStoreDescription() {
        return storeDescription;
    }

    public String getStoreCategory() {
        return storeCategory;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public String getStoreStatus() {
        return storeStatus;
    }

    public String getStoreLocation() {
        return storeLocation;
    }
}
