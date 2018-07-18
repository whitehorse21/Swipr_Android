package dk.techtify.swipr.model.profile;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.helper.FirebaseHelper;
import dk.techtify.swipr.model.ServerTime;
import dk.techtify.swipr.model.sell.Brand;
import dk.techtify.swipr.model.sell.ProductType;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/24/2017.
 */

public class IncomingBid extends Handler {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_CANCELED = 2;
    public static final int STATUS_DECLINED = 3;
    public static final int STATUS_ACCEPTED = 4;
    public static final int STATUS_EXPIRED = 5;

    String id;
    long bid;
    String bidderId;
    String sellerId;
    String productId;
    Brand brand;
    long created;
    long expirationTime;
    boolean includeShipping;
    long initialPrice;
    String message;
    String productPhotoUrl;
    long status;
    ProductType type;

    boolean mDeclineMessageOpen;
    String mDeclineMessage;

    @SuppressWarnings("unchecked")
    public IncomingBid(String key, Map<String, Object> map) {
        id = key;
        initialPrice = map.containsKey("initialPrice") ? (Long) map.get("initialPrice") : 0;
        bid = map.containsKey("bid") ? (Long) map.get("bid") : initialPrice;
        productId = map.containsKey("productId") ? map.get("productId").toString() : "";
        bidderId = map.containsKey("bidderId") ? map.get("bidderId").toString() : "";
        sellerId = map.containsKey("sellerId") ? map.get("sellerId").toString() : "";
        created = map.containsKey("created") ? (Long) map.get("created") : 0;
        expirationTime = map.containsKey("expirationTime") ? (Long) map.get("expirationTime") : 0;
        includeShipping = map.containsKey("includeShipping") && (Boolean) map.get("includeShipping");
        message = map.containsKey("message") ? map.get("message").toString() : null;
        productPhotoUrl = map.containsKey("productPhotoUrl") ? map.get("productPhotoUrl").toString() : "";
        status = map.containsKey("status") ? (Long) map.get("status") : 0;
        brand = new Brand(map.containsKey("brandId") ? map.get("brandId").toString() : "",
                map.containsKey("brandName") ? (HashMap<String, Object>) map.get("brandName") :
                        new HashMap<String, Object>());
        type = new ProductType();
        type.setId(map.containsKey("typeId") ? map.get("typeId").toString() : "");
        type.setLocalizedNames(map.containsKey("typeName") ? (HashMap<String, Object>) map.get(
                "typeName") : new HashMap<String, Object>());

        sendEmptyMessageDelayed(0, expiresIn());
    }

    public String getId() {
        return id;
    }

    public long getBid() {
        return bid;
    }

    public String getProductId() {
        return productId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public Brand getBrand() {
        return brand;
    }

    public long getCreated() {
        return created;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isIncludeShipping() {
        return includeShipping;
    }

    public long getInitialPrice() {
        return initialPrice;
    }

    public String getMessage() {
        return message;
    }

    public String getProductPhotoUrl() {
        return productPhotoUrl;
    }

    public long getStatus() {
        return status;
    }

    public ProductType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IncomingBid && ((IncomingBid) obj).getId().equals(id);
    }

    public boolean isDeclineMessageOpen() {
        return mDeclineMessageOpen;
    }

    public void setDeclineMessageOpen(boolean mDeclineMessageOpen) {
        this.mDeclineMessageOpen = mDeclineMessageOpen;
    }

    public String getDeclineMessage() {
        return mDeclineMessage;
    }

    public void setDeclineMessage(String mDeclineMessage) {
        this.mDeclineMessage = mDeclineMessage;
    }

    public long expiresIn() {
        return created + expirationTime - ServerTime.getServerTime();
    }

    public boolean hasExpired() {
        return created + expirationTime - ServerTime.getServerTime() < 0;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 0) {
            if (AppConfig.DEBUG) {
                Log.d(bidderId.equals(User.getLocalUser().getId()) ? "OUTGOING BID" : "INCOMING BID",
                        id + " has expired");
            }
            status = STATUS_EXPIRED;
            if (bidderId.equals(User.getLocalUser().getId())) {
                FirebaseHelper.expireOutgoingBid(this);
            } else {
                FirebaseHelper.expireIncomingBid(this);
            }
        }
    }
}
