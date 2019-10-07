package Helper;
/*Author : LEE THIAN XIN
Programme : RSD3
Year : 2018*/

import java.io.Serializable;

public class StoreOption implements Serializable {
    private int storeOption;
    private String storeOptionName;

    public StoreOption(int storeOption, String storeOptionName) {
        this.storeOption = storeOption;
        this.storeOptionName = storeOptionName;
    }

    public int getStoreOption() {
        return storeOption;
    }

    public String getStoreOptionName() {
        return storeOptionName;
    }
}
