package Helper;

import java.io.Serializable;

public class MenuOption implements Serializable {
    private int menuImage;
    private String menuName;

    public MenuOption(int menuImage, String menuName) {
        this.menuImage = menuImage;
        this.menuName = menuName;
    }

    public int getMenuImage() {
        return menuImage;
    }

    public String getMenuName() {
        return menuName;
    }
}
