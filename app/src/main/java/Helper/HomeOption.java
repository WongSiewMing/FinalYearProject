package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class HomeOption implements Serializable {

    private int homeOption;
    private String homeOptionName;

    public HomeOption(int homeOption, String homeOptionName) {
        this.homeOption = homeOption;
        this.homeOptionName = homeOptionName;
    }

    public int getHomeOption() {
        return homeOption;
    }

    public String getHomeOptionName() {
        return homeOptionName;
    }
}
