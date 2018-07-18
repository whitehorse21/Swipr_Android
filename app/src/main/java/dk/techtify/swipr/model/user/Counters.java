package dk.techtify.swipr.model.user;

import android.util.Log;

import java.util.Map;
import java.util.Observable;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.activity.BaseActivity;

/**
 * Created by Pavel on 1/20/2017.
 */

public class Counters extends Observable {

    private static Counters sInstance;

    public static Counters getInstance() {
        if (sInstance == null) {
            sInstance = new Counters();
        }
        return sInstance;
    }

    private long activeBidsSent;
    private long activePosts;
    private long followers;
    private long following;
    private long purchased;
    private long sold;

    private Counters() {

    }

    public long getActiveBidsReceived() {
        return BaseActivity.getIncomingBids().size();
    }

    public long getActiveBidsSent() {
        return activeBidsSent;
    }

    public long getActivePosts() {
        return activePosts;
    }

    public long getFollowers() {
        return followers;
    }

    public long getFollowing() {
        return following;
    }

    public long getPurchased() {
        return purchased;
    }

    public long getSold() {
        return sold;
    }

    public String getActiveBidsReceivedString() {
        return String.valueOf(BaseActivity.getIncomingBids().size());
    }

    public String getActiveBidsSentString() {
        return String.valueOf(activeBidsSent);
    }

    public String getActiveBidsString() {
        return String.valueOf(BaseActivity.getIncomingBids().size() + activeBidsSent);
    }

    public String getActivePostsString() {
        return String.valueOf(activePosts);
    }

    public String getFollowersString() {
        return String.valueOf(followers);
    }

    public String getFollowingString() {
        return String.valueOf(following);
    }

    public String getPurchasedString() {
        return String.valueOf(purchased);
    }

    public String getSoldString() {
        return String.valueOf(sold);
    }

    public void update(Map<String, Object> map) {
        if (AppConfig.DEBUG) {
            Log.d("COUNTERS", "Updated");
        }
        activePosts = map.containsKey("active-posts") ? (Long) map.get("active-posts") : 0;
        followers = map.containsKey("followers") ? (Long) map.get("followers") : 0;
        following = map.containsKey("following") ? (Long) map.get("following") : 0;
        purchased = map.containsKey("purchased") ? (Long) map.get("purchased") : 0;
        sold = map.containsKey("sold") ? (Long) map.get("sold") : 0;

        setChanged();
        notifyObservers();
    }

    public void updateOutgoingBidsCount(int count) {
        activeBidsSent = count;

        setChanged();
        notifyObservers();
    }

    public void increaseOutgoingBidsCount() {
        activeBidsSent += 1;

        setChanged();
        notifyObservers();
    }

    public void decreaseOutgoingBidsCount() {
        activeBidsSent -= 1;

        setChanged();
        notifyObservers();
    }
}
