package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;

public class StuffBasicInfoOB implements Serializable {
    private String stuffID;
    private String studentID;
    private String stuffName;
    private String stuffImage;
    private String stuffDescription;

    public StuffBasicInfoOB(String stuffID, String studentID, String stuffName, String stuffImage, String stuffDescription) {
        this.stuffID = stuffID;
        this.studentID = studentID;
        this.stuffName = stuffName;
        this.stuffImage = stuffImage;
        this.stuffDescription = stuffDescription;
    }

    public String getStuffID() {
        return stuffID;
    }

    public String getStudentID() {
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
}
