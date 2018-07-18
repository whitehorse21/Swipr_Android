package dk.techtify.swipr.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.SwiprApp;

/**
 * Created by Pavel on 5/3/2015.
 */
public class DisplayHelper {

    public static boolean isScreenSmall() {
        return SwiprApp.getInstance().getSp().getBoolean(Constants.Prefs.IS_SCREEN_SMALL, false);
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT > 20) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
        }
    }

    public static boolean hasNavigationBar(Context context) {
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && context.getResources().getBoolean(id);
    }

    public static int[] getScreenResolution(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int dpWidth = displayMetrics.widthPixels;
        int dpHeight = displayMetrics.heightPixels;

        return new int[]{dpWidth, dpHeight};
    }

    public static int dpToPx(Context context, int dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int pxToDp(Context context, int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context, int orientation) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }

    public static boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    public static boolean isXlarge(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        return xlarge;
    }

    @SuppressWarnings("ResourceType")
    public static int getActionBarHeight(Context context) {
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize, R.attr.actionBarSize};
        TypedArray a = context.obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        float heightMaterial = a.getDimension(1, -1);
        return (int) heightMaterial;
    }

    public static int getScreenOrientation(Activity activity) {
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() < getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }
}