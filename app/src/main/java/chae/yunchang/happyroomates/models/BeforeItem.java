package chae.yunchang.happyroomates.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class BeforeItem {
    private String docId;
    private Map<String, Object> items;
    private String paid;
    private float total;
    private float mymoney;
    private float matemoney;
    private Date boughtDate;

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    private String shop;


    public BeforeItem(){}

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Map<String, Object> getItems() {
        return items;
    }

    public void setItems(Map<String, Object> items) {
        this.items = items;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getMymoney() {
        return mymoney;
    }

    public void setMymoney(float mymoney) {
        this.mymoney = mymoney;
    }

    public float getMatemoney() {
        return matemoney;
    }

    public void setMatemoney(float matemoney) {
        this.matemoney = matemoney;
    }

    public Date getBoughtDate() {
        return boughtDate;
    }

    public void setBoughtDate(Date boughtDate) {
        this.boughtDate = boughtDate;
    }
}
