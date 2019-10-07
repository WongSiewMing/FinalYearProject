package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import java.io.Serializable;

public class StudentBasicInfoOB  implements Serializable {
    private String studentID;
    private String photo;
    private String studentName;
    private String studentProgramme;

    public StudentBasicInfoOB(String studentID, String photo, String studentName, String studentProgramme) {
        this.studentID = studentID;
        this.photo = photo;
        this.studentName = studentName;
        this.studentProgramme = studentProgramme;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getPhoto() {
        return photo;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentProgramme() {
        return studentProgramme;
    }
}
