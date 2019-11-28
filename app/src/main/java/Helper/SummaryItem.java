package Helper;


public class SummaryItem{
    private String itemNum;
    private String itemImage;
    private String itemID;
    private String itemName;
    private Double itemAmount;

    public SummaryItem(String itemNum, String itemImage, String itemID, String itemName, Double itemAmount){
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

    public Double getItemAmount() {
        return itemAmount;
    }

    public String getItemNum(){
        return itemNum;
    }

    public String getItemImage(){
        return  itemImage;
    }
}
