package Helper;



import java.io.Serializable;

public class getStuff implements Serializable{
    private String stuffID;
    private String stuffName;


    public getStuff(String stuffID, String stuffName) {
        this.stuffID = stuffID;
        this.stuffName = stuffName;
    }

    public String getStuffID() {
        return stuffID;
    }

    public String getStuffName() {
        return stuffName;
    }
}
