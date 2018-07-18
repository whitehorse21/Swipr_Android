package dk.techtify.swipr.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.onesignal.OneSignal;

import java.io.File;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.activity.LoginActivity;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.push.SwiprNotificationExtender;

/**
 * Created by Pavel on 5/31/2016.
 */
public class IntentHelper {

    public static void openAppSettings(Activity activity) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static void openWifiSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void openWifiSettings(Activity activity, int code) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, code);
    }

    public static void openUrl(Activity activity, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            DialogHelper.showDialogWithCloseAndDone(activity, R.string.warning,
                    R.string.error_app_not_found, null);
        }
    }

    public static void sendEmailViaGmail(Activity activity, String email, String subject) {
        try {
            Intent mailIntent = new Intent(Intent.ACTION_SEND);
            mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, new String[]{subject});
            mailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            mailIntent.setType("text/html");

            final PackageManager pm = activity.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(mailIntent, 0);
            String className = null;
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                    className = info.activityInfo.name;

                    if (className != null && !className.isEmpty()) {
                        break;
                    }
                }
            }
            mailIntent.setClassName("com.google.android.gm", className);

            activity.startActivity(mailIntent);
        } catch (Exception e) {
            Intent mailIntent = new Intent(Intent.ACTION_SEND);
            mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, new String[]{subject});
            mailIntent.setType("message/rfc822");
            activity.startActivity(mailIntent);
        }
    }

    public static void logOut(Activity activity) {
        if (NetworkHelper.isOnline(activity, NetworkHelper.ALERT)) {
            SwiprNotificationExtender.clearNotifications(activity);
            OneSignalHelper.unsubscribe();
            User.removeLocalUser();
            SpHelper.removeKeys();
            FirebaseAuth.getInstance().signOut();
            BaseActivity.setIncomingBids(null);
            BaseActivity.setOutgoingBids(null);
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }

    public static void shareFile(Context context, File file) {
        if (file == null) {
            DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                    R.string.couldnt_retrieve_attached_file, null);
            return;
        }

        final Intent shareIntent = ShareCompat.IntentBuilder.from((AppCompatActivity) context)
                .setType("image/*")
                .setStream(FileProvider.getUriForFile(context, context.getApplicationContext()
                        .getPackageName() + ".provider", file))
                .getIntent();
        try {
            context.startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                    R.string.error_app_not_found, null);
        }
    }

    public static void shareTextViaWhatsApp(Context context, String text) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
        boolean found = false;
        for (final ResolveInfo app : activityList) {
            if ((app.activityInfo.name).contains("com.whatsapp")) {
                final ActivityInfo activity = app.activityInfo;
                final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setComponent(name);
                context.startActivity(shareIntent);
                found = true;
                break;
            }
        }

        if (!found) {
            DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                    R.string.error_app_not_found, null);
        }
    }

    public static void shareTextViaFacebook(Context context, String text) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
        boolean found = false;
        for (final ResolveInfo app : activityList) {
            if ((app.activityInfo.name).contains("facebook")) {
                final ActivityInfo activity = app.activityInfo;
                final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setComponent(name);
                context.startActivity(shareIntent);
                found = true;
                break;
            }
        }

        if (!found) {
            DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                    R.string.error_app_not_found, null);
        }
    }

    public static void shareTextViaGmail(Context context, String text) {
        try {
            Intent mailIntent = new Intent(Intent.ACTION_SEND);
            mailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            mailIntent.setType("text/html");
            mailIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);

            final PackageManager pm = context.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(mailIntent, 0);
            String className = null;
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                    className = info.activityInfo.name;

                    if (className != null && !className.isEmpty()) {
                        break;
                    }
                }
            }
            mailIntent.setClassName("com.google.android.gm", className);

            context.startActivity(mailIntent);
        } catch (Exception e) {
            DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                    R.string.error_app_not_found, null);
        }
    }

    public static void shareTextViaSms(Context context, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);

            if (defaultSmsPackageName != null) {
                sendIntent.setPackage(defaultSmsPackageName);
            }
            try {
                context.startActivity(sendIntent);
            } catch (Exception e) {
                DialogHelper.showDialogWithCloseAndDone(context, R.string.warning,
                        R.string.error_app_not_found, null);
            }
        } else {
            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            smsIntent.putExtra("sms_body", text);
            context.startActivity(smsIntent);
        }
    }
}