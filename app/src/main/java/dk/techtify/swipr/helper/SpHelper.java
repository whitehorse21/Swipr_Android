package dk.techtify.swipr.helper;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.SwiprApp;

/**
 * Created by Pavel on 1/26/2017.
 */
public class SpHelper {

    public static void checkViewPreferences() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String todayKey = "views" + DateTimeHelper.getFormattedDate(System.currentTimeMillis(), "yyyyMMdd");
                Map<String, ?> entries = SwiprApp.getInstance().getSp().getAll();
                Set<String> keys = entries.keySet();
                for (String key : keys) {
                    if (key.contains("views") && !key.equals(todayKey)) {
                        SwiprApp.getInstance().getSp().edit().remove(key).apply();
                    }
                }
            }
        }).start();
    }

    public static void removeKeys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = SwiprApp.getInstance().getSp().edit();

                Map<String, ?> entries = SwiprApp.getInstance().getSp().getAll();
                Set<String> keys = entries.keySet();
                for (String key : keys) {
                    if (!key.equals(Constants.Prefs.IS_SCREEN_SMALL) && !key.equals(Constants.Prefs.CONTENT_HEIGHT)) {
                        editor.remove(key);
                    }
                }

                editor.apply();
            }
        }).start();
    }
}
