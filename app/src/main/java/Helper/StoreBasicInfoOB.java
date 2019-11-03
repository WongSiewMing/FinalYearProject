package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;

public class StoreBasicInfoOB implements Serializable {

    private String StoreID;
    private String StoreImg;
    private String StoreName;
    private String StoreDetail;
    private String StudentID;

    public StoreBasicInfoOB(String storeID, String storeImg, String storeName, String storeDetail, String studentID) {
        StoreID = storeID;
        StoreImg = storeImg;
        StoreName = storeName;
        StoreDetail = storeDetail;
        StudentID = studentID;
    }

    public String getStoreID() {
        return StoreID;
    }

    public String getStoreImg() {
        return StoreImg;
    }

    public String getStoreName() {
        return StoreName;
    }

    public String getStoreDetail() {
        return StoreDetail;
    }

    public String getStudentID() {
        return StudentID;
    }
}
