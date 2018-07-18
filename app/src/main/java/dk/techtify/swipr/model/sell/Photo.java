package dk.techtify.swipr.model.sell;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Pavel on 1/5/2017.
 */

public class Photo implements Serializable {

    String localPath;
    Bitmap bitmap;
    boolean fromGallery;

    public Photo() {

    }

    public Photo(String localPath, boolean fromGallery) {
        this.localPath = localPath;
        this.fromGallery = fromGallery;
    }

    public String getLocalPath() {
        return localPath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isFromGallery() {
        return fromGallery;
    }
}
