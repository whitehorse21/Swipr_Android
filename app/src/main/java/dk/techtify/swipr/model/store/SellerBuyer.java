package dk.techtify.swipr.model.store;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Map;

import dk.techtify.swipr.model.sell.ContactInfo;

/**
 * Created by Pavel on 1/11/2017.
 */

public class SellerBuyer implements Serializable {

    String id;
    String firstName;
    String lastName;
    String photoUrl;
    long ratingTotal;
    long ratingVotesNumber;
    boolean isPlusMember;
    long created;
    ContactInfo contactInfo;

    public SellerBuyer(String id, Map<String, Object> map) {
        this.id = id;
        this.firstName = map.containsKey("firstName") ? map.get("firstName").toString() : "";
        this.lastName = map.containsKey("lastName") ? map.get("lastName").toString() : "";
        this.photoUrl = map.containsKey("photoUrl") ? map.get("photoUrl").toString() : "";
        this.ratingTotal = map.containsKey("ratingTotal") ? (Long) map.get("ratingTotal") : 0;
        this.ratingVotesNumber = map.containsKey("ratingVotesNumber") ? (Long) map.get("ratingVotesNumber") : 0;
        this.isPlusMember = map.containsKey("isPlusMember") && (Boolean) map.get("isPlusMember");
        this.created = map.containsKey("created") ? (Long) map.get("created") : 0;
        if (map.containsKey("contactInfo")) {
           contactInfo = new ContactInfo((Map<String, Object>) map.get("contactInfo"));
        }
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Exclude
    public String getName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public long getRatingTotal() {
        return ratingTotal;
    }

    public long getRatingVotesNumber() {
        return ratingVotesNumber;
    }

    public float getRating() {
        if (ratingVotesNumber == 0) {
            return 0f;
        }
        return (float) ratingTotal / (float) ratingVotesNumber;
    }

    public boolean isPlusMember() {
        return isPlusMember;
    }

    public long getCreated() {
        return created;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }
}
