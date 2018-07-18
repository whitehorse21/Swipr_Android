package dk.techtify.swipr.model.store;

import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.model.sell.Brand;
import dk.techtify.swipr.model.sell.ProductType;

/**
 * Created by Pavel on 1/23/2017.
 */

public class OutgoingBid implements Serializable {

    int price;
    int timerHrsPosition;
    int timerMinPosition;
    String message;
    boolean includeShipping;

    String bidderId;
    String sellerId;
    String productId;
    String photoUrl;
    Brand brand;
    ProductType type;
    long initialPrice;

    public OutgoingBid() {
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTimerHrsPosition() {
        return timerHrsPosition;
    }

    public void setTimerHrsPosition(int timerHrsPosition) {
        this.timerHrsPosition = timerHrsPosition;
    }

    public int getTimerMinPosition() {
        return timerMinPosition;
    }

    public void setTimerMinPosition(int timerMinPosition) {
        this.timerMinPosition = timerMinPosition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIncludeShipping() {
        return includeShipping;
    }

    public void setIncludeShipping(boolean includeShipping) {
        this.includeShipping = includeShipping;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public long getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(long initialPrice) {
        this.initialPrice = initialPrice;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    private long getExpirationTime() {
        long hrs = timerHrsPosition * 1000 * 60 * 60;
        long min = timerMinPosition * 1000 * 60 * 5;
        return hrs + min == 0 ? Constants.DAY_IN_MILLIS / 2 : hrs + min;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 0);
        map.put("bid", price);
        map.put("expirationTime", getExpirationTime());
        if (message != null) {
            map.put("message", message);
        }
        map.put("includeShipping", includeShipping);
        map.put("bidderId", bidderId);
        map.put("sellerId", sellerId);
        map.put("productId", productId);
        map.put("productPhotoUrl", photoUrl);
        map.put("initialPrice", initialPrice);
        map.put("brandId", brand.getId());
        map.put("typeId", type.getId());

        Map<String, String> brandNameMap = new HashMap<>();
        brandNameMap.put("en", brand.getEn());
        map.put("brandName", brandNameMap);

        Map<String, String> typeNameMap = new HashMap<>();
        if (type.getEn() != null) {
            typeNameMap.put("en", type.getEn());
        }
        if (type.getDk() != null) {
            typeNameMap.put("dk", type.getDk());
        }
        map.put("typeName", typeNameMap);
        map.put("created", ServerValue.TIMESTAMP);

        return map;
    }
}
