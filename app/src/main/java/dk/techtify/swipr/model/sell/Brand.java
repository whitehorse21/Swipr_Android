package dk.techtify.swipr.model.sell;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel on 1/4/2017.
 */

public class Brand implements Serializable, SellProductTypeBrand {

    String id;
    String en;

    public Brand(String en) {
        this.en = en;
    }

    public Brand(String id, Map<String, Object> map) {
        this.id = id;
        this.en = map.containsKey("en") ? map.get("en").toString() : "";
    }

    public Brand(String id, JSONObject brandName) throws JSONException {
        if (brandName == null) {
            en = "";
            return;
        }
        en = brandName.has("en") ? brandName.getString("en") : "";
    }

    public String getId() {
        return id;
    }

    public String getEn() {
        return en;
    }

    @Override
    public String getName() {
        return en;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
