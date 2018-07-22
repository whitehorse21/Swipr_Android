package dk.techtify.swipr.activity.sell;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.sell.NewAddPhotoAdapter;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.PermissionHelper;
import dk.techtify.swipr.helper.PhotoHelper;
import dk.techtify.swipr.model.sell.Photo;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 12/10/2016.
 */
public class AddPhotoActivity extends AppCompatActivity implements NewAddPhotoAdapter.OnPhotoClickListener {

    public static final String EXTRA_PHOTO_LIST = "dk.techtify.swipr.activity.sell.AddPhotoActivity.EXTRA_PHOTO_LIST";
    public static final String EXTRA_PHOTO_LIST_INCOME = "dk.techtify.swipr.activity.sell.AddPhotoActivity.EXTRA_PHOTO_LIST_INCOME";

    private ActionView mActionView;
    private RecyclerView mRecycler;
    private NewAddPhotoAdapter mAdapter;
    private List<Photo> mPhotos;
    private Photo mMainPhoto;
    private File mJustCapturedPhotoFile;
    private ImageView mMainPhotoView;
    private ImageButton mMainPhotoRemove;
    private View mCamera, mGallery;

    public void setJustCapturedPhotoFile(File mJustCapturedPhotoFile) {
        this.mJustCapturedPhotoFile = mJustCapturedPhotoFile;
    }

    PhotoHelper.ExecuteListener mGetPhotoExecuteListener = this::attachPhoto;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PhotoHelper.REQUEST_GALLERY) {
            if (resultCode != RESULT_OK) {
                return;
            }
            PhotoHelper.retrievePathFromUri(this, data.getData(), true, mGetPhotoExecuteListener);
        } else if (requestCode == PhotoHelper.REQUEST_CAMERA) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (mJustCapturedPhotoFile == null || !mJustCapturedPhotoFile.exists()) {
                DialogHelper.showDialogWithCloseAndDone(this, R.string.error, R.string.photo_could_not_be_taken, null);
                return;
            }
            PhotoHelper.retrievePathFromUri(this, Uri.fromFile(mJustCapturedPhotoFile), false,
                    mGetPhotoExecuteListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        mActionView = findViewById(R.id.action_view);
        mActionView.findViewById(R.id.menu).setVisibility(View.INVISIBLE);
        mActionView.setTitle(getString(R.string.take_photos));
        mActionView.setActionButton(R.drawable.ic_close, this::onBackPressed);
        mActionView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        if (mPhotos == null) {
            mPhotos = new ArrayList<>();
        }

        mRecycler = findViewById(R.id.recycler);
        mAdapter = new NewAddPhotoAdapter(this, mPhotos, this);
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecycler.setAdapter(mAdapter);

        mGallery = findViewById(R.id.gallery);
        mGallery.setOnClickListener(v -> PhotoHelper.checkGalleryPermission(AddPhotoActivity.this));

        mCamera = findViewById(R.id.camera);
        mCamera.setOnClickListener(v -> PhotoHelper.checkCameraAvailability(AddPhotoActivity.this));

        findViewById(R.id.next).setOnClickListener(v -> {
            if (mMainPhoto == null || mMainPhoto.getLocalPath() == null) {
                DialogHelper.showDialogWithCloseAndDone(AddPhotoActivity.this, R.string.warning,
                        R.string.add_at_least_one_photo, null);
                return;
            }
            mMainPhoto.setBitmap(null);
            ArrayList<Photo> photos = new ArrayList<>();
            photos.add(mMainPhoto);
            for (Photo p : mPhotos) {
                if (p.getLocalPath() != null) {
                    p.setBitmap(null);
                    photos.add(p);
                }
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_PHOTO_LIST, photos);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        mMainPhotoView = findViewById(R.id.main_photo);
        mMainPhotoRemove = findViewById(R.id.main_remove);
        mMainPhotoRemove.setOnClickListener(v -> DialogHelper.showDialogWithCloseAndDone(AddPhotoActivity.this, R.string.warning,
                R.string.remove_photo, new DialogHelper.OnActionListener() {
                    @Override
                    public void onPositive(Object o) {
                        setMainPhoto(null);
                    }
                }));

        if (getIntent().hasExtra(EXTRA_PHOTO_LIST_INCOME)) {
            ArrayList<Photo> localPhotos = (ArrayList<Photo>) getIntent().getSerializableExtra(EXTRA_PHOTO_LIST_INCOME);
            for (int i = 0; i < localPhotos.size(); i++) {
                Photo photo = localPhotos.get(i);
                if (i == 0) {
                    setMainPhoto(photo);
                } else {
                    mPhotos.add(photo);
                    if (photo.getBitmap() == null) {
                        getThumbnail(photo.getLocalPath(), false);
                    }
                }
            }
            setMaxVisibility(mPhotos.size() >= AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT - 1);
        }
    }

    public void attachPhoto(String path, boolean fromGallery) {
        if (mMainPhoto == null) {
            setMainPhoto(new Photo(path, fromGallery));
            return;
        }
        if (mMainPhoto.getLocalPath() != null && mMainPhoto.getLocalPath().equals(path)) {
            DialogHelper.showDialogWithCloseAndDone(this, R.string.warning,
                    R.string.this_photo_already_added, null);
            return;
        }
        for (Photo p : mPhotos) {
            if (p.getLocalPath() != null && p.getLocalPath().equals(path)) {
                DialogHelper.showDialogWithCloseAndDone(this, R.string.warning,
                        R.string.this_photo_already_added, null);
                return;
            }
        }
        mPhotos.add(new Photo(path, fromGallery));
        setMaxVisibility(mPhotos.size() >= AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT - 1);
        mAdapter.notifyDataSetChanged();
        getThumbnail(path, fromGallery);
    }

    private void setMainPhoto(Photo photo) {
        mMainPhoto = photo;
        if (photo == null) {
            mMainPhotoView.setImageDrawable(null);
            mMainPhotoRemove.setVisibility(View.INVISIBLE);

            if (mPhotos.size() > 0) {
                setMainPhoto(mPhotos.get(0));
                mAdapter.removeItem(0);
            }

            setMaxVisibility(mPhotos.size() >= AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT - 1);
        } else {
            mMainPhotoRemove.setVisibility(View.VISIBLE);
            BitmapHelper.createThumbnailFromPhoto(500, photo.getLocalPath(), mThumbnailCompleteMainListener);
        }
    }

    private void getThumbnail(String path, boolean fromGallery) {
        if (fromGallery) {
            BitmapHelper.getThumbnailFromPhoto(this, path, mThumbnailCompleteListener);
        } else {
            BitmapHelper.createThumbnailFromPhoto(path, mThumbnailCompleteListener);
        }
    }

    private BitmapHelper.LoadCompleteListener mThumbnailCompleteMainListener = new BitmapHelper
            .LoadCompleteListener() {
        @Override
        public void onLoadComplete(String path, Bitmap bitmap) {
            mMainPhotoView.setAlpha(0f);
            mMainPhotoView.animate().alpha(1f).setDuration(300).start();
            mMainPhoto.setBitmap(bitmap);
            mMainPhotoView.setImageBitmap(bitmap);
        }

        @Override
        public void onError() {

        }
    };

    private BitmapHelper.LoadCompleteListener mThumbnailCompleteListener = new BitmapHelper
            .LoadCompleteListener() {
        @Override
        public void onLoadComplete(String path, Bitmap bitmap) {
            for (Photo photo : mPhotos) {
                if (photo.getLocalPath() != null && photo.getLocalPath().equals(path)) {
                    photo.setBitmap(bitmap);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
            }
        }

        @Override
        public void onError() {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean allPermissionsGranted = PermissionHelper.allPermissionsGranted(grantResults);
        if (requestCode == PhotoHelper.REQUEST_PERMISSION_GALLERY) {
            if (allPermissionsGranted) {
                PhotoHelper.openGallery(this);
            } else {
                PhotoHelper.accessDeniedAlert(this, true);
            }
        } else if (requestCode == PhotoHelper.REQUEST_PERMISSION_CAMERA) {
            if (allPermissionsGranted) {
                PhotoHelper.openCamera(this);
            } else {
                PhotoHelper.accessDeniedAlert(this, false);
            }
        }
    }

    @Override
    public void onRemoveClick(final int position) {
        DialogHelper.showDialogWithCloseAndDone(this, R.string.warning, R.string.remove_photo,
                new DialogHelper.OnActionListener() {
                    @Override
                    public void onPositive(Object o) {
                        mAdapter.removeItem(position);
                        setMaxVisibility(mPhotos.size() >= AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT - 1);
                    }
                });
    }

    private void setMaxVisibility(boolean isMaxReached) {
        mGallery.setVisibility(isMaxReached ? View.INVISIBLE : View.VISIBLE);
        mCamera.setVisibility(isMaxReached ? View.INVISIBLE : View.VISIBLE);
    }
}
