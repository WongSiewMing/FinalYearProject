package Helper;

public class SummaryItem {
    private String itemNum;
    private int itemImage;
    private String itemID;
    private String itemName;
    private String itemAmount;

    public SummaryItem(String itemNum, int itemImage, String itemID, String itemName, String itemAmount){
        this.itemNum = itemNum;
        this.itemImage = itemImage;
        this.itemID = itemID;
        this.itemName = itemName;
        this.itemAmount = itemAmount;
    }

    public String getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public String getItemNum(){
        return itemNum;
    }

    public int getItemImage(){
        return  itemImage;
    }
}
