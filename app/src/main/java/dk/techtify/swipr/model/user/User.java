package dk.techtify.swipr.model.user;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.SwiprApp;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.IntentHelper;
import dk.techtify.swipr.model.sell.ContactInfo;

/**
 * Created by graforlov on 9/7/2016.
 */
public class User {

    @Exclude
    String id;
    String firstName;
    String lastName;
    String email;
    int gender;
    String photoUrl;
    boolean isPlusMember;
    int ratingTotal;
    int ratingVotesNumber;
    Object created;
    ContactInfo contactInfo;
    String plusSubscriptionAndroidId;

    private User() {
        if (SwiprApp.getInstance().getSp().contains(Constants.Prefs.USER)) {
            String string = SwiprApp.getInstance().getSp().getString(Constants.Prefs.USER, "");
            JSONObject jo;
            try {
                jo = new JSONObject(string);
                id = jo.has("id") ? jo.getString("id") : "";
                firstName = jo.has("firstName") ? jo.getString("firstName") : "";
                lastName = jo.has("lastName") ? jo.getString("lastName") : "";
                email = jo.has("email") ? jo.getString("email") : "";
                gender = jo.has("gender") ? jo.getInt("gender") : 0;
                photoUrl = jo.has("photoUrl") ? jo.getString("photoUrl") : null;
                isPlusMember = jo.has("isPlusMember") && jo.getString("isPlusMember").equals("true");
                ratingTotal = jo.has("ratingTotal") ? jo.getInt("ratingTotal") : 0;
                ratingVotesNumber = jo.has("ratingVotesNumber") ? jo.getInt("ratingVotesNumber") : 0;
                created = jo.has("created") ? jo.getLong("created") : 0;
                contactInfo = jo.has("contactInfo") ? new ContactInfo(jo.getJSONObject("contactInfo")) : null;
                plusSubscriptionAndroidId = jo.has("plusSubscriptionAndroidId") ? jo.getString("plusSubscriptionAndroidId") : null;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static User getLocalUser() {
        return new User();
    }

    public static void removeLocalUser() {
        SwiprApp.getInstance().getSp().edit()
                .remove(Constants.Prefs.USER)
                .apply();
    }

    @Exclude
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

    public int getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Exclude
    public boolean isPlusMember() {
        return isPlusMember;
    }

    public int getRatingTotal() {
        return ratingTotal;
    }

    public int getRatingVotesNumber() {
        return ratingVotesNumber;
    }

    @Exclude
    public float getRating() {
        if (ratingVotesNumber == 0) {
            return 0f;
        }
        return ratingTotal / ratingVotesNumber;
    }

    @Exclude
    public long getCreatedAt() {
        if (created == null) {
            return 0;
        }
        return (Long) created;
    }

    @Exclude
    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public String getPlusSubscriptionAndroidId() {
        return plusSubscriptionAndroidId;
    }

    public static void saveLocalUser(String id, HashMap<String, Object> map) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("id", id != null ? id : "");
            jo.put("firstName", map.containsKey("firstName") ? map.get("firstName").toString() : "");
            jo.put("lastName", map.containsKey("lastName") ? map.get("lastName").toString() : "");
            jo.put("email", map.containsKey("email") ? map.get("email").toString() : "");
            jo.put("gender", map.containsKey("gender") ? map.get("gender") : 0);
            if (map.containsKey("photoUrl")) {
                jo.put("photoUrl", map.get("photoUrl"));
            }
            jo.put("isPlusMember", map.containsKey("isPlusMember") && (Boolean) map.get("isPlusMember"));
            jo.put("ratingTotal", map.containsKey("ratingTotal") ? map.get("ratingTotal") : 0);
            jo.put("ratingVotesNumber", map.containsKey("ratingVotesNumber") ? map.get("ratingVotesNumber") : 0);
            jo.put("created", map.containsKey("created") ? map.get("created") : 0);
            if (map.containsKey("contactInfo")) {
                jo.put("contactInfo", (new ContactInfo((Map<String, Object>) map.get("contactInfo"))).toJson());
            }
            if (map.containsKey("plusSubscriptionAndroidId")) {
                jo.put("plusSubscriptionAndroidId", map.get("plusSubscriptionAndroidId"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwiprApp.getInstance().getSp().edit().putString(Constants.Prefs.USER, jo.toString()).apply();
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean checkSignIn(final Activity activity) {
        boolean result = true;
        if (User.getLocalUser().getEmail() == null) {
            result = false;
            DialogHelper.showDialogWithCloseAndDone(activity, R.string.warning,
                    R.string.sign_in_to_make_this_action, new DialogHelper.OnActionListener() {
                        @Override
                        public void onPositive(Object o) {
                            IntentHelper.logOut(activity);
                        }
                    });
        }
        return result;
    }

    public static void refreshLocalUser() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("user-data").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            User.saveLocalUser(uid, (HashMap<String, Object>) dataSnapshot.getValue());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void updateContactInfo(ContactInfo ci) {
        if (SwiprApp.getInstance().getSp().contains(Constants.Prefs.USER)) {
            String string = SwiprApp.getInstance().getSp().getString(Constants.Prefs.USER, "");
            JSONObject jo;
            try {
                jo = new JSONObject(string);

                JSONObject contactInfo = new JSONObject();
                contactInfo.put("street", ci.getStreet());
                contactInfo.put("postCode", ci.getPostCode());
                contactInfo.put("city", ci.getCity());
                contactInfo.put("mobile", ci.getMobile());
                contactInfo.put("lat", ci.getLat());
                contactInfo.put("lng", ci.getLng());

                jo.put("contactInfo", contactInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            SwiprApp.getInstance().getSp().edit().putString(Constants.Prefs.USER, jo.toString()).apply();
        }
    }

    public static Map<String, Object> mapOfNewUser(String firstName, String lastName, String email, String photoUrl, int gender) {
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("email", email);
        map.put("gender", gender);
        if (photoUrl != null) {
            map.put("photoUrl", photoUrl);
        }
        map.put("isPlusMember", false);
        map.put("ratingTotal", 0);
        map.put("ratingTotal", 0);
        map.put("ratingVotesNumber", 0);
        map.put("created", ServerValue.TIMESTAMP);
        map.put("locale", Locale.getDefault().getLanguage());
        return map;
    }

    public void setPlusMember(boolean isPlus) {
        if (SwiprApp.getInstance().getSp().contains(Constants.Prefs.USER)) {
            String string = SwiprApp.getInstance().getSp().getString(Constants.Prefs.USER, "");
            JSONObject jo;
            try {
                jo = new JSONObject(string);
                jo.put("isPlusMember", isPlus);
                SwiprApp.getInstance().getSp().edit().putString(Constants.Prefs.USER, jo.toString()).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPlusSubscriptionId(String orderId) {
        if (SwiprApp.getInstance().getSp().contains(Constants.Prefs.USER)) {
            String string = SwiprApp.getInstance().getSp().getString(Constants.Prefs.USER, "");
            JSONObject jo;
            try {
                jo = new JSONObject(string);
                jo.put("plusSubscriptionAndroidId", orderId);
                SwiprApp.getInstance().getSp().edit().putString(Constants.Prefs.USER, jo.toString()).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}