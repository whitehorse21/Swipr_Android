package dk.techtify.swipr.helper;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.activity.sell.AddPhotoActivity;

/**
 * Created by Pavel on 1/5/2017.
 */

public class PhotoHelper {

    public static final int REQUEST_PERMISSION_GALLERY = 1101;
    public static final int REQUEST_GALLERY = 1102;
    public static final int REQUEST_PERMISSION_CAMERA = 1103;
    public static final int REQUEST_CAMERA = 1104;

    public static void checkGalleryPermission(final FragmentActivity activity) {
        PermissionHelper.requestPermissions(activity,
                REQUEST_PERMISSION_GALLERY,
                () -> openGallery(activity), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void openGallery(FragmentActivity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            activity.startActivityForResult(intent, REQUEST_GALLERY);
        } catch (ActivityNotFoundException e) {
            DialogHelper.showDialogWithCloseAndDone(activity, R.string.warning,
                    R.string.couldnot_find_apps_for_this_action, null);
        }
    }

    public static void checkCameraAvailability(FragmentActivity activity) {
        PackageManager pm = activity.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            PhotoHelper.checkCameraPermission(activity);
        } else {
            DialogHelper.showDialogWithCloseAndDone(activity, R.string.warning,
                    R.string.device_doesnt_have_camera, null);
        }
    }

    public static void checkCameraPermission(final FragmentActivity activity) {
        PermissionHelper.requestPermissions(activity,
                REQUEST_PERMISSION_CAMERA,
                () -> openCamera(activity), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void openCamera(FragmentActivity activity) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(activity.getPackageManager()) != null) {
            File justCapturedPhotoFile = UriHelper.createImageFile();
            if (justCapturedPhotoFile == null) {
                DialogHelper.showDialogWithCloseAndDone(activity, R.string.error, R.string.photo_could_not_be_taken, null);
                return;
            }

            if (Build.VERSION.SDK_INT > 20) {
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity,
                        activity.getPackageName() + ".provider", justCapturedPhotoFile));
            } else {
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(justCapturedPhotoFile));
            }

            if (activity instanceof MainActivity) {
                ((MainActivity) activity).setJustCapturedPhotoFile(justCapturedPhotoFile);
            } else if (activity instanceof AddPhotoActivity) {
                ((AddPhotoActivity) activity).setJustCapturedPhotoFile(justCapturedPhotoFile);
            }

            activity.startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
        }
    }

    public static void retrievePathFromUri(FragmentActivity activity, Uri uri, boolean fromGallery,
                                           ExecuteListener listener) {
        String path;
        if (Build.VERSION.SDK_INT < 19) {
            if (uri.toString().contains("content://com.google.android.apps.photos.content")) {
                path = UriHelper.getPath(activity, uri);
            } else {
                path = UriHelper.getPathFromContentUri(activity, uri);
            }
        } else {
            path = UriHelper.getPath(activity, uri);
        }

        if (AppConfig.DEBUG) {
            Log.d("FILE FROM GALLERY", "path=" + path);
        }

        if (path == null) {
            DialogHelper.showDialogWithCloseAndDone(activity, R.string.error, R.string.file_could_not_be_opened, null);
            return;
        }

        listener.onPostExecute(path, fromGallery);
    }

    public static void accessDeniedAlert(final FragmentActivity activity, boolean isGallery) {
        DialogHelper.showDialogWithCloseAndDone(activity, R.string.warning,
                isGallery ? R.string.please_grant_us_a_permission_gallery :
                        R.string.please_grant_us_a_permission_camera,
                new DialogHelper.OnActionListener() {
                    @Override
                    public void onPositive(Object o) {
                        IntentHelper.openAppSettings(activity);
                    }
                });
    }

    public interface ExecuteListener {
        void onPostExecute(String path, boolean fromGallery);
    }
}
