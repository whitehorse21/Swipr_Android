package dk.techtify.swipr.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import dk.techtify.swipr.AppConfig;

/**
 * Created by Pavel on 11/15/2015.
 */
public class InternetStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                if (AppConfig.DEBUG) {
                    Log.d("NETWORK", "WIFI " + activeNetwork.isConnected());
                }
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (AppConfig.DEBUG) {
                    Log.d("NETWORK", "MOBILE " + activeNetwork.isConnected());
                }
            }
        }
    }
}
