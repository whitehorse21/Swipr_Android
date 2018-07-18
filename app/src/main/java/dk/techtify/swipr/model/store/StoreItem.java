package dk.techtify.swipr.model.store;

import java.io.Serializable;

import dk.techtify.swipr.helper.StringHelper;

/**
 * Created by Pavel on 12/31/2016.
 */

public class StoreItem implements Serializable {

    String userId;
    String userName;
    float userRating;
    boolean plusMember;

    String itemId;
    String itemName;
    int itemRes;
    String itemSize;
    int itemPrice;
    String itemLocation;

    public StoreItem(String userId, String userName, float userRating, boolean plusMember, String itemName, int itemRes,
                     String itemSize, int itemPrice, String itemLocation) {
        this.userId = userId;
        this.userName = userName;
        this.userRating = userRating;
        this.plusMember = plusMember;

        this.itemId = StringHelper.generateId();
        this.itemName = itemName;
        this.itemRes = itemRes;
        this.itemSize = itemSize;
        this.itemPrice = itemPrice;
        this.itemLocation = itemLocation;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public float getUserRating() {
        return userRating;
    }

    public boolean isPlusMember() {
        return plusMember;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemRes() {
        return itemRes;
    }

    public String getItemSize() {
        return itemSize;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public String getItemLocation() {
        return itemLocation;
    }
}
