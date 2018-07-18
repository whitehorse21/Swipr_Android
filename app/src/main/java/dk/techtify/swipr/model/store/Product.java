package dk.techtify.swipr.model.store;

import com.google.firebase.database.Exclude;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import dk.techtify.swipr.model.sell.Brand;
import dk.techtify.swipr.model.sell.ContactInfo;
import dk.techtify.swipr.model.sell.ProductType;

/**
 * Created by Pavel on 1/11/2017.
 */

public class Product {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_DELETED = 1;

    String id;
    String userId;
    Brand brand;
    String categoryId;
    long created;
    ArrayList<String> tags;
    ProductType type;
    String description;
    String size;
    ContactInfo contactInfo;
    ArrayList<String> photos;
    long price;
    long visibleMode;
    long views;
    long status;
    boolean ratingLeftBySeller;
    boolean ratingLeftByBuyer;
    boolean isAddedToFavorites;

    @SuppressWarnings("unchecked")
    public Product(String id, Map<String, Object> map) {
        this.id = id;
        this.userId = map.containsKey("userId") ? map.get("userId").toString() : "";

        this.brand = new Brand(map.containsKey("brandId") ? map.get("brandId").toString() : "",
                (Map<String, Object>) map.get("brandName"));
        this.categoryId = map.containsKey("categoryId") ? map.get("categoryId").toString() : "";
        this.created = map.containsKey("created") ? Long.parseLong(map.get("created").toString()) : 0;
        this.tags = map.containsKey("tags") ? (ArrayList<String>) map.get("tags") : null;
        this.type = new ProductType((Map<String, Object>) map.get("typeName"),
                map.containsKey("typeId") ? map.get("typeId").toString() : "");
        this.description = map.containsKey("description") ? map.get("description").toString() : "";
        this.size = map.containsKey("size") ? map.get("size").toString() : "";
        this.contactInfo = map.containsKey("contactInfo") ? new ContactInfo((Map<String, Object>)
                map.get("contactInfo")) : null;
        this.photos = map.containsKey("photos") ? (ArrayList<String>) map.get("photos") : null;
        this.price = map.containsKey("price") ? Long.parseLong(map.get("price").toString()) : 0;
        this.visibleMode = map.containsKey("visibleMode") ? Long.parseLong(map.get("visibleMode").toString()) : 0;
        this.views = map.containsKey("views") ? Long.parseLong(map.get("views").toString()) : 0;
        this.status = map.containsKey("status") ? Long.parseLong(map.get("status").toString()) : 0;
        this.ratingLeftBySeller = map.containsKey("ratingLeftBySeller") ? (Boolean) map.get("ratingLeftBySeller") : false;
        this.ratingLeftByBuyer = map.containsKey("ratingLeftByBuyer") ? (Boolean) map.get("ratingLeftByBuyer") : false;
    }

    public Product(JSONObject jo) throws JSONException {
        this.id = jo.has("id") ? jo.getString("id") : "";
        this.userId = jo.has("userId") ? jo.getString("userId") : "";
        this.brand = new Brand(jo.has("brandId") ? jo.getString("brandId") : "",
                jo.has("brandName") && !(jo.get("brandName").equals(null)) ? jo.getJSONObject("brandName") : null);
        this.categoryId = jo.has("categoryId") ? jo.getString("categoryId") : "";
        this.created = jo.has("created") ? jo.getLong("created") : 0;
        if (jo.has("tags")) {
            JSONArray tagsJa = jo.getJSONArray("tags");
            if (tagsJa.length() > 0) {
                this.tags = new ArrayList<>();
                for (int i = 0; i < tagsJa.length(); i++) {
                    this.tags.add(tagsJa.getString(i));
                }
            }
        }
        this.type = new ProductType(jo.has("productType") ? jo.getString("productType") : "",
                jo.has("productTypeName") && !(jo.get("productTypeName").equals(null)) ? jo.getJSONObject("productTypeName") : null);
        this.description = jo.has("description") ? jo.getString("description") : "";
        this.size = jo.has("size") ? jo.getString("size") : "";
        this.contactInfo = jo.has("contactInfo") ? new ContactInfo(jo.getJSONObject("contactInfo")) : null;
        if (jo.has("photos")) {
            JSONArray photosJa = jo.getJSONArray("photos");
            if (photosJa.length() > 0) {
                this.photos = new ArrayList<>();
                for (int i = 0; i < photosJa.length(); i++) {
                    this.photos.add(photosJa.getString(i));
                }
            }
        }
        this.price = jo.has("price") ? jo.getInt("price") : 0;
        this.visibleMode = jo.has("visibleMode") ? jo.getInt("visibleMode") : 0;
        this.isAddedToFavorites = jo.has("isAddedToFavorites") && jo.getBoolean("isAddedToFavorites");
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public String getSize() {
        return size;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public long getPrice() {
        return price;
    }

    public long getVisibleMode() {
        return visibleMode;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public long getCreated() {
        return created;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public long getViews() {
        return views;
    }

    public Brand getBrand() {
        return brand;
    }

    public ProductType getType() {
        return type;
    }

    public long getStatus() {
        return status;
    }

    @Exclude
    public boolean isRatingLeftBySeller() {
        return ratingLeftBySeller;
    }

    @Exclude
    public boolean isRatingLeftByBuyer() {
        return ratingLeftByBuyer;
    }

    @Exclude
    public String getName() {
        return brand.getName() + " " + type.getName();
    }

    @Exclude
    public boolean isAddedToFavorites() {
        return isAddedToFavorites;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Product) {
            Product toCompare = (Product) o;
            return this.id.equals(toCompare.id);
        }
        return false;
    }
}
