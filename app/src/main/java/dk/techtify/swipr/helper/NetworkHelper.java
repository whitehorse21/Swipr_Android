package dk.techtify.swipr.helper;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import dk.techtify.swipr.R;

/**
 * Created by Pavel on 6/4/2015.
 */
public class NetworkHelper {

    public static final int NONE = 0;
    public static final int TOAST = 1;
    public static final int ALERT = 2;
    public static final int SNACK_BAR = 3;

    public static boolean isOnline(final Context context, final int mode) {
        return isOnline(context, mode, null);
    }

    public static boolean isOnline(final Context context, View snackBarView) {
        return isOnline(context, SNACK_BAR, true, snackBarView, null);
    }

    public static boolean isOnline(final Context context, final int mode, final DialogHelper.OnActionListener listener) {
        return isOnline(context, mode, true, listener);
    }

    public static boolean isOnline(final Context context, final int mode, final boolean cancelable, final DialogHelper.OnActionListener listener) {
        return isOnline(context, mode, cancelable, null, listener);
    }

    public static boolean isOnline(final Context context, final int mode, final boolean cancelable,
                                   View snackBarRoot, final DialogHelper.OnActionListener listener) {
        if (!hasInternetConnectivity(context)) {
            if (mode == TOAST) {
                Toast.makeText(context, context.getResources().getString(
                        R.string.error_internet_connection).replace(".", ""),
                        Toast.LENGTH_LONG).show();
            } else if (mode == ALERT) {
                DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                        R.string.error_internet_connection, new DialogHelper.OnActionListener() {
                            @Override
                            public void onPositive(Object o) {
                                if (listener != null) {
                                    listener.onPositive(null);
                                }
                                if (cancelable) {
                                    IntentHelper.openWifiSettings(((Activity) context));
                                } else {
                                    IntentHelper.openWifiSettings(((Activity) context), 100);
                                }
                            }

                            @Override
                            public void onNegative(Object o) {
                                if (listener != null) {
                                    listener.onNegative(null);
                                }
                            }
                        });
            } else if (mode == SNACK_BAR && snackBarRoot != null) {
                Snackbar snackbar = Snackbar
                        .make(snackBarRoot, context.getString(R.string.error_internet_connection), Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.go_to_settings), view -> IntentHelper.openWifiSettings((Activity) context));

                snackbar.show();
            }
            return false;
        }
        return true;
    }

    private static boolean hasInternetConnectivity(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}