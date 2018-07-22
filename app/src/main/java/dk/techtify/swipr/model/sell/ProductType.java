package dk.techtify.swipr.model.sell;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Pavel on 1/4/2017.
 */

public class ProductType implements Serializable, SellProductTypeBrand {

    String id;
    String dk;
    String en;
    ArrayList<String> scaleIds;
    ArrayList<String> categoryIds;

    public ProductType() {

    }

    public ProductType(String en) {
        this.en = this.dk = en;
    }

    public ProductType(Map<String, Object> map, String id) {
        this.id = id;
        this.dk = map.containsKey("dk") ? map.get("dk").toString() : "";
        this.en = map.containsKey("en") ? map.get("en").toString() : "";
    }

    public ProductType(String id, Map<String, Object> map) {
        this.id = id;
        if (map.containsKey("name")) {
            Map<String, Object> nameMap = (Map<String, Object>) map.get("name");
            this.dk = nameMap.containsKey("dk") ? nameMap.get("dk").toString() : "";
            this.en = nameMap.containsKey("en") ? nameMap.get("en").toString() : "";
        } else {
            this.dk = this.en = "";
        }
        this.scaleIds = map.containsKey("scaleType") ? ((ArrayList<String>) map.get("scaleType")) : null;
        this.categoryIds = map.containsKey("category") ? ((ArrayList<String>) map.get("category")) : new ArrayList<>();
    }

    public ProductType(String id, JSONObject jo) throws JSONException {
        this.id = id;
        if (jo == null) {
            this.en = this.dk = "";
            return;
        }
        this.en = jo.has("en") ? jo.getString("en") : "";
        this.dk = jo.has("dk") ? jo.getString("dk") : "";
    }

    public String getId() {
        return id;
    }

    public String getDk() {
        return dk;
    }

    public String getEn() {
        return en;
    }

    public ArrayList<String> getScaleType() {
        return scaleIds;
    }

    public ArrayList<String> getCategoryIds() {
        return categoryIds;
    }

    @Override
    public String getName() {
        return Locale.getDefault().getLanguage().equals("dk") ? dk : en;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setLocalizedNames(HashMap<String, Object> map) {
        this.dk = map.containsKey("dk") ? map.get("dk").toString() : "";
        this.en = map.containsKey("en") ? map.get("en").toString() : "";
    }
}
