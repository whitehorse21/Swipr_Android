package dk.techtify.swipr;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.onesignal.OneSignal;

import dk.techtify.swipr.billing.IabHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Pavel on 12/10/2016.
 */

public class SwiprApp extends Application {

    private static SwiprApp sInstance;
    private static SharedPreferences sSharedPreferences;
    private static IabHelper sIabHelper;

    public static SwiprApp getInstance() {
        if (sInstance == null) {
            sInstance = new SwiprApp();
        }
        return sInstance;
    }

    public SharedPreferences getSp() {
        return sSharedPreferences;
    }

    public IabHelper getIabHelper() {
        return sIabHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        OneSignal.startInit(this).init();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder().schemaVersion(1).build());

        if (!getSp().contains(Constants.Prefs.IS_SCREEN_SMALL)) {
            getSp().edit().putBoolean(Constants.Prefs.IS_SCREEN_SMALL, DisplayHelper.pxToDp(this,
                    DisplayHelper.getScreenResolution(this)[1]) < 561).apply();
        }

        sIabHelper = new IabHelper(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2lFFm1sY+BPIqlS+x435ynM9m6re13P5RF0exXlMU2d4s2g8Yc2kdXynmOgoXZ631inZcibtxF5F2APFopTR/CT3PYQ26ESE81GF8KjvzPG559wwSerz8M2NDrKEvlyvT6BGeziqByUq2DjsciPctdSwjW7sHaoKAj1eXqzI8nCjAo6YM58q+Za8rrVXaNRbnqxt4tbc7LY/JsFFtFZB8czXopt1SRm/S1v1DSkV6Cg/99WixMgsS7Qw03Q8NT9jIl1ucp4iXwAgHE9YylSfMdWAJg1lNO9O3UyE0gvFksbaH5GbOQVEGgtmZrirAUekMRwxEB2UdmFFH7kud5G2vQIDAQAB");
    }
}