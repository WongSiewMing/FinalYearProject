package Helper;

import java.io.Serializable;

public class ChatOption implements Serializable {

    private int chatOption;
    private String chatOptionName;

    public ChatOption (int chatOption, String chatOptionName) {
        this.chatOption = chatOption;
        this.chatOptionName = chatOptionName;
    }

    public int getChatOption() {
        return chatOption;
    }

    public String getChatOptionName() {
        return chatOptionName;
    }

}
