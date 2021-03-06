package cpen391_21.stegocrypto.DataTransferHistory;

// A DataTransferHistoryItem that stores info for our custom data transfer history ListView
public class DataTransferHistoryItem {
    public String action;
    public String username;
    public String date;
    public String dataPackageID;

    public DataTransferHistoryItem(String action, String username, String date, String dataPackageID){
        this.action = action;
        this.username = username;
        this.date = date;
        this.dataPackageID = dataPackageID;
    }
}