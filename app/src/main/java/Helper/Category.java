package Helper;

import java.io.Serializable;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Category implements Serializable {
    private int categoryIcon;
    private String categoryName;

    public Category(int categoryIcon, String categoryName) {
        this.categoryIcon = categoryIcon;
        this.categoryName = categoryName;
    }

    public int getCategoryIcon() {
        return categoryIcon;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
