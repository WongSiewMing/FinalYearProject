package Helper;

import java.io.Serializable;

/*Author : Wong Qing Li
Programme : RSD3
Year : 2019*/

public class SummaryOption implements Serializable {
    private int summaryOption;
    private String summaryOptionName;

    public SummaryOption(int summaryOption, String summaryOptionName){
        this.summaryOption = summaryOption;
        this.summaryOptionName = summaryOptionName;
    }

    public int getSummaryOption(){
        return summaryOption;
    }

    public String getSummaryOptionName(){
        return summaryOptionName;
    }

}
