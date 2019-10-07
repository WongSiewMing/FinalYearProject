package Helper;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import java.io.Serializable;

public class SearchOB implements Serializable {
    private String userImage;
    private String userName;
    private String userID;

    public SearchOB(String userImage, String userName, String userID) {
        this.userImage = userImage;
        this.userName = userName;
        this.userID = userID;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserID() {
        return userID;
    }
}
