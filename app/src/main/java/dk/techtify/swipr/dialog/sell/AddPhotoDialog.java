package dk.techtify.swipr.dialog.sell;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.sell.AddPhotoAdapter;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.PhotoHelper;
import dk.techtify.swipr.model.sell.Photo;

/**
 * Created by Pavel on 1/4/2017.
 */

public class AddPhotoDialog extends BaseDialog implements AddPhotoAdapter.OnPhotoClickListener {

    private RecyclerView mRecycler;
    private AddPhotoAdapter mAdapter;

    private PhotoSelectListener mPhotoSelectListener;

    public void setPhotoSelectListener(PhotoSelectListener mPhotoSelectListener) {
        this.mPhotoSelectListener = mPhotoSelectListener;
    }

    private List<Photo> mPhotos;

    public void setPhotos(List<Photo> localPhotos) {
        if (mPhotos == null) {
            mPhotos = new ArrayList<>();
        }
        for (Photo photo : localPhotos) {
            mPhotos.add(photo);
            if (photo.getBitmap() == null) {
                getThumbnail(photo.getLocalPath(), false);
            }
        }
        if (mPhotos.size() < AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT) {
            mPhotos.add(new Photo());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sell_add_photo, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> getDialog().dismiss());

        if (mPhotos == null) {
            mPhotos = new ArrayList<>();
            mPhotos.add(new Photo());
        }

        mRecycler = view.findViewById(R.id.recycler);
        mAdapter = new AddPhotoAdapter(getActivity(), mPhotos, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 2 : 1;
            }
        });
        mRecycler.setLayoutManager(gridLayoutManager);
        mRecycler.setAdapter(mAdapter);

        view.findViewById(R.id.positive).setOnClickListener(view1 -> {
            ArrayList<Photo> photos = new ArrayList<>();
            for (Photo p : mPhotos) {
                if (p.getLocalPath() != null) {
                    photos.add(p);
                }
            }
            if (photos.size() == 0) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.add_at_least_one_photo, null);
                return;
            }
            mPhotoSelectListener.onPhotoSelected(photos);

            getDialog().dismiss();
        });

        return view;
    }

    @Override
    public void onRemoveClick(final int position) {
        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning, R.string.remove_photo,
                new DialogHelper.OnActionListener() {
                    @Override
                    public void onPositive(Object o) {
                        mAdapter.removeItem(position);
                    }
                });
    }

    @Override
    public void onAddClick() {
        DialogHelper.showAddPhotoDialog(getActivity(), new DialogHelper.OnActionListener() {
            @Override
            public void onPositive(Object o) {
                getDialog().getWindow().getAttributes().windowAnimations = 0;

                if ((Integer) o == 0) {
                    PhotoHelper.checkGalleryPermission(getActivity());
                } else {
                    PackageManager pm = getActivity().getPackageManager();
                    if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        PhotoHelper.checkCameraPermission(getActivity());
                    } else {
                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                R.string.device_doesnt_have_camera, null);
                    }
                }
            }
        });
    }

    public void attachPhoto(String path, boolean fromGallery) {
        for (Photo p : mPhotos) {
            if (p.getLocalPath() != null && p.getLocalPath().equals(path)) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.this_photo_already_added, null);
                return;
            }
        }
        mPhotos.add(mPhotos.size() - 1, new Photo(path, fromGallery));
        if (mPhotos.size() == AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT + 1) {
            mPhotos.remove(mPhotos.size() - 1);
        }
        mAdapter.notifyDataSetChanged();
        getThumbnail(path, fromGallery);
    }

    private void getThumbnail(String path, boolean fromGallery) {
        if (fromGallery) {
            BitmapHelper.getThumbnailFromPhoto(getActivity(), path, mThumbnailCompleteListener);
        } else {
            BitmapHelper.createThumbnailFromPhoto(path, mThumbnailCompleteListener);
        }
    }

    private BitmapHelper.LoadCompleteListener mThumbnailCompleteListener = new BitmapHelper
            .LoadCompleteListener() {
        @Override
        public void onLoadComplete(String path, Bitmap bitmap) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogSlidingAnimation;

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

    public interface PhotoSelectListener {
        void onPhotoSelected(ArrayList<Photo> photos);
    }
}
