package dk.techtify.swipr.model;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.SwiprApp;

/**
 * Created by Pavel on 1/25/2017.
 */

public class ServerTime {

    public static long getServerTime() {
        return System.currentTimeMillis() + SwiprApp.getInstance().getSp().getLong(Constants.Prefs
                .SERVER_TIME_OFFSET, 0);
    }

    public static void setOffset(double offset) {
        SwiprApp.getInstance().getSp().edit()
                .putLong(Constants.Prefs.SERVER_TIME_OFFSET, (long) offset)
                .apply();
    }
}
