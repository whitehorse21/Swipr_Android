package dk.techtify.swipr.model.sell;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel on 1/5/2017.
 */

public class ContactInfo implements Serializable {

    String street;
    String postCode;
    String city;
    String mobile;
    double lat;
    double lng;

    public ContactInfo(String street, String postCode, String city, String mobile, double lat, double lng) {
        this.street = street;
        this.postCode = postCode;
        this.city = city;
        this.mobile = mobile;
        this.lat = lat;
        this.lng = lng;
    }

    public ContactInfo(JSONObject contactInfo) throws JSONException {
        this.street = contactInfo.getString("street");
        this.postCode = contactInfo.getString("postCode");
        this.city = contactInfo.getString("city");
        this.mobile = contactInfo.getString("mobile");
        this.lat = contactInfo.has("lat") ? contactInfo.getDouble("lat") : 0.0;
        this.lng = contactInfo.has("lng") ? contactInfo.getDouble("lng") : 0.0;
    }

    public ContactInfo(Map<String, Object> map) {
        this.street = map.containsKey("street") ? map.get("street").toString() : "";
        this.postCode = map.containsKey("postCode") ? map.get("postCode").toString() : "";
        this.city = map.containsKey("city") ? map.get("city").toString() : "";
        this.mobile = map.containsKey("mobile") ? map.get("mobile").toString() : "";
        this.lat = map.containsKey("lat") ? Double.valueOf(map.get("lat").toString()) : 0.0;
        this.lng = map.containsKey("lng") ? Double.valueOf(map.get("lng").toString()) : 0.0;
    }

    public String getStreet() {
        return street;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCity() {
        return city;
    }

    public String getMobile() {
        return mobile;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("street", street);
        map.put("postCode", postCode);
        map.put("city", city);
        map.put("mobile", mobile);
        map.put("lat", lat);
        map.put("lng", lng);

        return map;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("street", street);
        jo.put("postCode", postCode);
        jo.put("city", city);
        jo.put("mobile", mobile);
        jo.put("lat", lat);
        jo.put("lng", lng);
        return jo;
    }
}
