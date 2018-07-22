package dk.techtify.swipr.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pavel on 4/28/2015.
 */
public class UriHelper {

    public static InputStream fileToInputStream(Uri uri) {
        InputStream is = null;

        try {
            is = new FileInputStream(uri.toString());

            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;
    }

    public static String saveBitmapToStorage(Context context, Bitmap bitmapImage, String fileName, boolean onlyInternal, Bitmap.CompressFormat bitmapCompressFormat) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());

        File directory;

        boolean savedToExternalStorage;
        if (onlyInternal) {
            // path to /data/data/yourapp/app_data/imageDir
            directory = context.getApplicationContext().getCacheDir();
            savedToExternalStorage = false;
        } else {
            if (isExternalMemoryAvailable()) {
                directory = context.getApplicationContext().getExternalCacheDir();

//                File destFolder = new File(directory + "");
//                if (!destFolder.exists() || !destFolder.isDirectory()) {
//                    destFolder.mkdirs();
//                }
//                directory = destFolder;

                savedToExternalStorage = true;
            } else {
                directory = context.getApplicationContext().getCacheDir();
                savedToExternalStorage = false;
            }
        }

        File path = new File(directory, fileName);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path);
            bitmapImage.compress(bitmapCompressFormat, 100, fos);
            fos.close();

//            if (savedToExternalStorage) {
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.TITLE, fileName);
////                values.put(MediaStore.Images.Media.DESCRIPTION, AppConfig.PHOTO_DESCRIPTION_FOR_GALLERY);
//                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//                values.put(MediaStore.Images.ImageColumns.BUCKET_ID, context.toString().toLowerCase(Locale.US).hashCode());
//                values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, path.getName().toLowerCase(Locale.US));
//                values.put("_data", path.getAbsolutePath());
//
//                ContentResolver cr = context.getContentResolver();
//                cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return path.getAbsolutePath();
    }

    public static boolean isExternalMemoryAvailable() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if (mExternalStorageAvailable && mExternalStorageWriteable) {
            return true;
        }
        return false;
    }

    public static String getPathFromContentUri(Context context, Uri uri) {
        String path = uri.getPath();
        if (uri.toString().startsWith("content://")) {
            String[] projection = {MediaStore.MediaColumns.DATA};
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            Cursor cursor = cr.query(uri, projection, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return path;
    }

    public static String fileName(String path) {
        String name;
        Uri uri = Uri.parse(path);
        name = uri.getLastPathSegment();

        return name;
    }

    public static File createImageFile() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "PNG_" + timeStamp + "_";
            if (isExternalMemoryAvailable()) {
                File storageDir = Environment.getExternalStorageDirectory();
                File image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".png",         /* suffix */
                        storageDir      /* directory */
                );

                return image;
            } else {
                File storageDir = Environment.getDataDirectory();
                File image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".png",         /* suffix */
                        storageDir      /* directory */
                );

                return image;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getExtension(Uri uri) {
        String[] a = uri.getLastPathSegment().split("\\.");
        return a[a.length - 1];
    }

    public static String getMimeTypeOfFile(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        return cr.getType(uri) ;
    }
}
