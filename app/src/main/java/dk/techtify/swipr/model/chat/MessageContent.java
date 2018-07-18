package dk.techtify.swipr.model.chat;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Pavel on 1/25/2017.
 */

public class MessageContent implements Serializable, Cloneable {

    public static final int TYPE_PRODUCT_MESSAGE = 0;
    public static final int TYPE_NEW_BID = 1;
    public static final int TYPE_BID_DECLINED = 2;
    public static final int TYPE_BID_CANCELED = 3;
    public static final int TYPE_BID_ACCEPTED = 4;

    int type;
    Object data;

    public MessageContent(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public MessageContent(Map<String, Object> map) {
        type = ((Long) map.get("type")).intValue();
        if (map.containsKey("data")) {
            Map<String, Object> contentMap = (Map<String, Object>) map.get("data");
            if (contentMap != null && contentMap.containsKey("id") && contentMap.containsKey(
                    "sellerId")) {
                data = new MessageContentDataProduct(contentMap);
            }
        }
    }

    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
