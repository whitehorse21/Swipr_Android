package dk.techtify.swipr.helper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Pavel on 12/13/2016.
 */

public class BitmapHelper {

    public static Bitmap getBitmapFromView(View view) {
        view.buildDrawingCache(true);
        Bitmap bitmap = view.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return bitmap;
    }

    public static void getBitmapFromUrl(final String path, final LoadCompleteListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    listener.onError();
                } else {
                    listener.onLoadComplete(path, bitmap);
                }
            }
        }.execute();

    }

    public static void getThumbnailFromPhoto(final FragmentActivity activity, final String path,
                                             final LoadCompleteListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap bitmap = null;
                File file = new File(path);
                if (file.exists()) {
                    Cursor ca = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{
                            MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
                    if (ca != null && ca.moveToFirst()) {
                        int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
                        ca.close();
                        bitmap = MediaStore.Images.Thumbnails.getThumbnail(activity.getContentResolver(),
                                id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                    }

                    ca.close();
                }

                bitmap = rotatedBitmap(path, bitmap);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    listener.onError();
                } else {
                    listener.onLoadComplete(path, bitmap);
                }
            }
        }.execute();
    }

    public static void createThumbnailFromPhoto(final String path,
                                                final LoadCompleteListener listener) {
        createThumbnailFromPhoto(350, path, listener);
    }

    public static void createThumbnailFromPhoto(final int sideSize, final String path,
                                                final LoadCompleteListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap bitmap = null;
                File file = new File(path);
                if (file.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path, options),
                            sideSize, sideSize);
                    bitmap = rotatedBitmap(path, bitmap);
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    listener.onError();
                } else {
                    listener.onLoadComplete(path, bitmap);
                }
            }
        }.execute();
    }

    public static File saveBitmapToCache(Context context, Bitmap bitmap) {
        File direct;
        try {
            direct = context.getExternalCacheDir();
        } catch (Exception e) {
            return null;
        }

        File file = new File(direct, "sellPreview.png");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap rotatedBitmap(String path, Bitmap bitmap) {
        if (path == null || bitmap == null) {
            return bitmap;
        }
        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Matrix matrix2 = new Matrix();
                matrix2.postRotate(180);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix2, true);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                Matrix matrix3 = new Matrix();
                matrix3.postRotate(270);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix3, true);
                break;
        }
        return bitmap;
    }

    public interface LoadCompleteListener {
        void onLoadComplete(String path, Bitmap bitmap);

        void onError();
    }
}
