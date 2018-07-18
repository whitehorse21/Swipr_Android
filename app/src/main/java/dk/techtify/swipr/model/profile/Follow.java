package dk.techtify.swipr.model.profile;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel on 1/20/2017.
 */

public class Follow implements Serializable {

    @Exclude
    String id;
    String name;
    String photoUrl;
    @Exclude
    boolean isFollowing;

    public Follow(String id, String name, String photoUrl) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.isFollowing = true;
    }

    public Follow(Object key, Map<String, Object> map, boolean isFollowing) {
        id = key.toString();
        name = map.containsKey("name") ? map.get("name").toString() : "";
        photoUrl = map.containsKey("photoUrl") ? map.get("photoUrl").toString() : null;
        this.isFollowing = isFollowing;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Exclude
    public boolean isFollowing() {
        return isFollowing;
    }

    @Exclude
    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("photoUrl", photoUrl);
        return map;
    }
}
