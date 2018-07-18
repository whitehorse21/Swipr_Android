package dk.techtify.swipr.helper;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.SwiprApp;
import dk.techtify.swipr.model.push.OutgoingPush;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/28/2017.
 */

public class OneSignalHelper {

    public static void subscribe() {
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                if (AppConfig.DEBUG) {
                    Log.d("ONE SIGNAL", "id is " + userId);
                }
                FirebaseDatabase.getInstance().getReference("user-data/" + User.getLocalUser().getId() +
                        "/push-tokens/" + userId).setValue(1);
            }
        });
    }

    public static void unsubscribe() {
        String id = SwiprApp.getInstance().getSp().getString(Constants.Prefs.ONE_SIGNAL_ID, "");
        FirebaseDatabase.getInstance().getReference("user-data/" + User.getLocalUser().getId() +
                "/push-tokens/" + id).removeValue();
        OneSignal.setSubscription(false);
    }

    public static void sendPush(final OutgoingPush outgoingPush) {
        FirebaseDatabase.getInstance().getReference("user-data/" + outgoingPush.getRecipientId() +
                "/push-tokens/").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    return;
                }

                List<String> tokens = new ArrayList<>();
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    tokens.add(pair.getKey().toString());
                }
                if (tokens.size() > 0) {
                    sendPush(outgoingPush, tokens);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static void sendPush(OutgoingPush outgoingPush, List<String> tokens) {
        try {
            JSONObject jo = new JSONObject();

            if (outgoingPush.getTitleDk() != null) {
                JSONObject titleJo = new JSONObject();
                titleJo.put("en", outgoingPush.getTitleEn());
                titleJo.put("da", outgoingPush.getTitleDk());
                jo.put("headings", titleJo);
            }

            JSONObject contentJo = new JSONObject();
            contentJo.put("en", outgoingPush.getTextEn());
            contentJo.put("da", outgoingPush.getTextDk());
            jo.put("contents", contentJo);

            JSONObject dataJo = new JSONObject();
            dataJo.put("type", outgoingPush.getType());
            if (outgoingPush.getSenderPhotoUrl() != null) {
                dataJo.put("senderPhotoUrl", outgoingPush.getSenderPhotoUrl());
            }
            jo.put("data", dataJo);

            JSONArray playersJa = new JSONArray();
            for (String token : tokens) {
                playersJa.put(token);
            }
            jo.put("include_player_ids", playersJa);
            OneSignal.postNotification(jo, new OneSignal.PostNotificationResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    if (AppConfig.DEBUG) {
                        Log.d("SENT PUSH", response.toString());
                    }
                }

                @Override
                public void onFailure(JSONObject response) {
                    if (AppConfig.DEBUG) {
                        Log.d("SENT PUSH", response.toString());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}