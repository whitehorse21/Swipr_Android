package dk.techtify.swipr.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.model.chat.MessageContent;

/**
 * Created by Pavel on 1/28/2017.
 */

public class SwiprNotificationExtender extends NotificationExtenderService {

    private static final String GROUP_KEY = "com.techtify.swipr.push.SwiprNotificationExtender.GROUP_KEY";

    public static android.support.v4.app.NotificationCompat.InboxStyle sInboxNotification;
    public static int sLineCount = 0;
    private static final int GROUP_ID = 2145000000;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult n) {
        JSONObject jo = n.payload.additionalData;
        int type = MessageContent.TYPE_PRODUCT_MESSAGE;
        try {
            type = jo.getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (type == MessageContent.TYPE_NEW_BID && n.isAppInFocus) {
            return true;
        }

        String senderPhotoUrl = getSenderPhotoUrl(n);

        Context context = getApplicationContext();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        int notificationId = R.string.app_name + ((int) (Math.random() * 100000));

        Intent clearOneNotifIntent = new Intent(this, NotificationBroadcastReceiver.class);
        clearOneNotifIntent.putExtra("id", notificationId);
        PendingIntent clearOneNotifPendIntent = PendingIntent.getBroadcast(context, 0, clearOneNotifIntent, 0);

        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(context);
        ncomp.setContentTitle(n.payload.title);
        ncomp.setContentText(n.payload.body);
        ncomp.setTicker(n.payload.body);
        ncomp.setGroup(GROUP_KEY);
        ncomp.setSmallIcon(R.drawable.ic_stat_onesignal_default);
        ncomp.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        ncomp.setAutoCancel(true);
        ncomp.setStyle(new NotificationCompat.BigTextStyle().bigText(n.payload.body));
        ncomp.setContentIntent(pendingIntent);
        ncomp.setDeleteIntent(clearOneNotifPendIntent);

        Notification notification = ncomp.build();
        notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE |
                Notification.DEFAULT_SOUND;
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notification);

        if (senderPhotoUrl != null) {
            loadPhoto(context, notificationId, notification, senderPhotoUrl);
        }

        if (sInboxNotification == null) {
            sInboxNotification = new NotificationCompat.InboxStyle();
            sInboxNotification.addLine(type == MessageContent.TYPE_PRODUCT_MESSAGE ? n.payload.title : n.payload.title + ": " + n.payload.body);
            sLineCount = 1;
        } else {
            Intent openIntent = new Intent(this, NotificationBroadcastReceiver.class);
            openIntent.putExtra("open", true);
            Intent deleteIntent = new Intent(this, NotificationBroadcastReceiver.class);
            openIntent.putExtra("delete", true);

            PendingIntent openPendingIntent = PendingIntent.getBroadcast(context, 0, openIntent, 0);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

            sInboxNotification.addLine(type == MessageContent.TYPE_PRODUCT_MESSAGE ? n.payload.title : n.payload.title + ": " + n.payload.body);
            sLineCount += 1;
            sInboxNotification.setBigContentTitle(sLineCount + " " + context.getString(R.string.new_notifications));
            sInboxNotification.setBigContentTitle(sLineCount + " " + context.getString(R.string.new_notifications));

            Notification summaryNotification = new NotificationCompat.Builder(context)
                    .setContentTitle(sLineCount + " " + context.getString(R.string.new_notifications))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setStyle(sInboxNotification)
                    .setGroup(GROUP_KEY)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
//                    .setDeleteIntent(deletePendingIntent)
                    .setContentIntent(pendingIntent)
                    .build();

            notificationManager.notify(GROUP_ID, summaryNotification);
        }
        return true;
    }

    private void loadPhoto(Context context, int id, Notification notification, String senderPhotoUrl) {
        int size = DisplayHelper.dpToPx(context, 64);
        try {
            Bitmap b = Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(senderPhotoUrl))
                    .asBitmap().into(size, size).get();
            notification.largeIcon = b;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(id, notification);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private String getSenderPhotoUrl(OSNotificationReceivedResult n) {
        if (n.payload.additionalData != null) {
            JSONObject jo = n.payload.additionalData;
            if (jo.has("senderPhotoUrl")) {
                try {
                    return jo.getString("senderPhotoUrl");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    public static void clearNotifications(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
        SwiprNotificationExtender.sInboxNotification = null;
        SwiprNotificationExtender.sLineCount = 0;
    }

}
