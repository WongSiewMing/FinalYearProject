package Helper;

import java.io.Serializable;

public class HistoryOption implements Serializable {

    private int historyOption;
    private String historyOptionName;

    public HistoryOption(int historyOption, String historyOptionName) {
        this.historyOption = historyOption;
        this.historyOptionName = historyOptionName;
    }

    public int getHistoryOption() {
        return historyOption;
    }

    public String getHistoryOptionName() {
        return historyOptionName;
    }
}
