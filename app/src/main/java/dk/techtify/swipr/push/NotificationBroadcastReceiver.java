package dk.techtify.swipr.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import dk.techtify.swipr.activity.MainActivity;

/**
 * Created by Pavel on 1/30/2017.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("id")) {
            NotificationManagerCompat.from(context).cancel(intent.getIntExtra("id", 0));
            SwiprNotificationExtender.sLineCount -= 1;
        }
        NotificationManagerCompat.from(context).cancelAll();
        SwiprNotificationExtender.sInboxNotification = null;
        SwiprNotificationExtender.sLineCount = 0;
        if (intent.hasExtra("open")) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
