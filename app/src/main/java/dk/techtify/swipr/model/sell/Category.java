package dk.techtify.swipr.model.sell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Pavel on 1/4/2017.
 */

public class Category implements Serializable {

    String id;
    String dk;
    String en;

    public Category(Object key, Map<String, Object> value) {
        id = key.toString();
        dk = value.containsKey("dk") ? value.get("dk").toString() : "";
        en = value.containsKey("en") ? value.get("en").toString() : "";
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

    public String getName() {
        return Locale.getDefault().getLanguage().equals("dk") ? dk : en;
    }
}
