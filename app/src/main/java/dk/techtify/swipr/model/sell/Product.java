package dk.techtify.swipr.model.sell;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/4/2017.
 */

public class Product implements Serializable {

    public static final int VISIBILITY_MODE_REGULAR = 0;
    public static final int VISIBILITY_MODE_PAID_BOOSTER = 1;
    public static final int VISIBILITY_MODE_PLUS_BOOSTER = 2;

    ProductType productType;
    Brand brand;
    String size;
    @Exclude
    int sizePosition = -1;
    @Exclude
    int sizeScalePosition = -1;
    ContactInfo contactInfo;
    ArrayList<Photo> localPhotos;
    String description;
    int price;
    int visibilityMode;
    String userId;
    String categoryId;
    ArrayList<String> tags;

    public Product() {
        userId = User.getLocalUser().getId();
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public ArrayList<Photo> getLocalPhotos() {
        return localPhotos;
    }

    public void setLocalPhotos(ArrayList<Photo> photoLocalPaths) {
        this.localPhotos = photoLocalPaths;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getTitle() {
        return (brand != null ? brand.getName() : "") + " " + (productType != null ? productType.getName() : "");
    }

    public int getVisibilityMode() {
        return visibilityMode;
    }

    public void setVisibilityMode(int visibilityMode) {
        this.visibilityMode = visibilityMode;
    }

    public String getUserId() {
        return userId;
    }

    @Exclude
    public int getSizePosition() {
        return sizePosition;
    }

    public void setSizePosition(int sizePosition) {
        this.sizePosition = sizePosition;
    }

    @Exclude
    public int getSizeScalePosition() {
        return sizeScalePosition;
    }

    public void setSizeScalePosition(int sizeScalePosition) {
        this.sizeScalePosition = sizeScalePosition;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }

    public Map<String, Object> toMap(String id, ArrayList<String> photos) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("typeId", productType.getId());

        Map<String, String> typeNameMap = new HashMap<>();
        if (productType.getEn() != null) {
            typeNameMap.put("en", productType.getEn());
        }
        if (productType.getDk() != null) {
            typeNameMap.put("dk", productType.getDk());
        }
        map.put("typeName", typeNameMap);

        map.put("brandId", brand.getId());

        Map<String, String> brandNameMap = new HashMap<>();
        brandNameMap.put("en", brand.getEn());
        map.put("brandName", brandNameMap);
        if (size != null) {
            map.put("size", size);
        }
        map.put("contactInfo", contactInfo.toMap());
        map.put("description", description);
        map.put("price", price);
        map.put("visibilityMode", visibilityMode);
        map.put("photos", photos);
        map.put("userId", userId);
        map.put("views", 0);
        map.put("categoryId", categoryId);
        if (tags != null) {
            map.put("tags", tags);
        }
        map.put("created", ServerValue.TIMESTAMP);
        map.put("status", 0);

        return map;
    }

    public JSONObject toJson(String id, ArrayList<String> photos) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("id", id);
        jo.put("brandId", brand.getId());

        JSONObject brandJo = new JSONObject();
        brandJo.put("en", brand.getEn());
        jo.put("brandName", brandJo);

        jo.put("categoryId", categoryId);
        jo.put("contactInfo", contactInfo.toJson());
        jo.put("description", description);

        JSONArray photoJa = new JSONArray();
        for (String photo : photos) {
            photoJa.put(photo);
        }
        jo.put("photos", photoJa);

        jo.put("price", price);
        jo.put("productType", productType.getId());

        JSONObject typeJo = new JSONObject();
        if (productType.getEn() != null) {
            typeJo.put("en", productType.getEn());
        }
        if (productType.getDk() != null) {
            typeJo.put("dk", productType.getDk());
        }
        jo.put("productTypeName", typeJo);

        if (size != null) {
            jo.put("size", size);
        }

        if (tags != null) {
            JSONArray tagsJa = new JSONArray();
            for (String tag : tags) {
                tagsJa.put(tag);
            }
            jo.put("tags", tagsJa);
        }
        jo.put("userId", userId);
        jo.put("visibilityMode", visibilityMode);

        return jo;
    }
}
