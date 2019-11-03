package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class ResetCode implements Serializable {
    private String resetPasswordID;
    private String StudentID;
    private String userEmail;
    private String resetCode;

    public ResetCode(String resetPasswordID, String StudentID, String userEmail, String resetCode) {
        this.resetPasswordID = resetPasswordID;
        this.StudentID = StudentID;
        this.userEmail = userEmail;
        this.resetCode = resetCode;
    }

    public String getResetPasswordID() {
        return resetPasswordID;
    }

    public String getStudentID() {
        return StudentID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getResetCode() {
        return resetCode;
    }

}
