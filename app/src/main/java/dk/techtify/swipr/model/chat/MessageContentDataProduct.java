package dk.techtify.swipr.model.chat;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Pavel on 1/25/2017.
 */

public class MessageContentDataProduct implements Serializable {

    String id;
    String sellerId;
    String photoUrl;

    public MessageContentDataProduct(String id, String sellerId) {
        this.id = id;
        this.sellerId = sellerId;
    }

    public MessageContentDataProduct(String id, String sellerId, String photoUrl) {
        this.id = id;
        this.sellerId = sellerId;
        this.photoUrl = photoUrl;
    }

    public MessageContentDataProduct(Map<String, Object> map) {
        id = map.get("id").toString();
        sellerId = map.get("sellerId").toString();
        if (map.containsKey("photoUrl")) {
            photoUrl = map.get("photoUrl").toString();
        }
    }

    public String getId() {
        return id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getSellerId() {
        return sellerId;
    }
}
