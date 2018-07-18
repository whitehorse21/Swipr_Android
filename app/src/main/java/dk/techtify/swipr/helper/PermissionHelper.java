package dk.techtify.swipr.helper;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pavel on 4/4/2016.
 */
public class PermissionHelper {

    public static void requestPermissions(FragmentActivity activity, int requestCode, PermissionsChecker permissionsChecker, String... permissions) {
        List<String> requestedPermissions = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                requestedPermissions.add(p);
            }
        }
        if (requestedPermissions.size() == 0) {
            permissionsChecker.allPermissionsGranted();
            return;
        }

        ActivityCompat.requestPermissions(activity, requestedPermissions.toArray(new String[requestedPermissions.size()]), requestCode);
    }

    public interface PermissionsChecker {
        void allPermissionsGranted();
    }

    public static boolean allPermissionsGranted(int[] grantResults) {
        boolean allPermissionsGranted = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        return allPermissionsGranted;
    }
}