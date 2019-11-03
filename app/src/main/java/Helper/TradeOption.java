package Helper;

import java.io.Serializable;

public class TradeOption implements Serializable {

    private int tradeOption;
    private String tradeOptionName;

    public TradeOption (int tradeOption, String tradeOptionName) {
        this.tradeOption = tradeOption;
        this.tradeOptionName = tradeOptionName;
    }

    public int getTradeOption() {
        return tradeOption;
    }

    public String getTradeOptionName() {
        return tradeOptionName;
    }

}
