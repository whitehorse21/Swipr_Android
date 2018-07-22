package dk.techtify.swipr.fragment.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cocosw.bottomsheet.BottomSheet;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.LoginActivity;
import dk.techtify.swipr.dialog.login.TermsConditionsDialog;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.helper.PermissionHelper;
import dk.techtify.swipr.view.GenderSelectorView;

/**
 * Created by Pavel on 12/10/2016.
 */

public class CreateProfileFragment extends Fragment implements BitmapHelper.LoadCompleteListener {

    private EditText mFirstName, mLastName, mEmail, mPassword;
    private GenderSelectorView mGender;
    private ImageView mPhoto;

    private boolean isPhotoEmpty = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(DisplayHelper.isScreenSmall() ? R.layout.small_fragment_create_profile : R.layout.fragment_create_profile, null);

        view.findViewById(R.id.back).setOnClickListener(view13 -> getActivity().onBackPressed());

        mPhoto = view.findViewById(R.id.photo);
        mPhoto.setOnClickListener(view12 -> showImageDialog());

        mFirstName = view.findViewById(R.id.first_name);
        mLastName = view.findViewById(R.id.last_name);
        mEmail = view.findViewById(R.id.email);
        mPassword = view.findViewById(R.id.password);
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        mGender = view.findViewById(R.id.gender);

        view.findViewById(R.id.next).setOnClickListener(view1 -> {
            if (mFirstName.getText().toString().trim().length() < 2) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.enter_first_name, null);
            } else if (mLastName.getText().toString().trim().length() < 2) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.enter_last_name, null);
            } else if (!IoHelper.isEmailValid(mEmail.getText().toString().trim())) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.enter_email, null);
            } else if (mPassword.getText().toString().length() < 6) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.enter_password, null);
            } else if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                IoHelper.hideKeyboard(getActivity(), mPassword);
                ((LoginActivity) getActivity()).registrationWithEmailAndPassword(
                        mFirstName.getText().toString().trim(),
                        mLastName.getText().toString().trim(),
                        mEmail.getText().toString().trim(),
                        mPassword.getText().toString().trim(),
                        mGender.getGender());
            } else {
                IoHelper.hideKeyboard(getActivity(), mPassword);
            }
        });

        view.findViewById(R.id.terms).setOnClickListener(v -> {
            TermsConditionsDialog tcd = new TermsConditionsDialog();
            tcd.show(getActivity().getSupportFragmentManager(), tcd.getClass().getSimpleName());
        });

        return view;
    }

    private void showImageDialog() {
        BottomSheet.Builder sheet = new BottomSheet.Builder(getActivity());
        sheet.sheet(R.id.sheet_photo_camera, null, getResources().getString(R.string.camera));
        sheet.sheet(R.id.sheet_photo_gallery, null, getResources().getString(R.string.gallery));
        if (!isPhotoEmpty) {
            sheet.sheet(R.id.sheet_photo_remove, null, getResources().getString(R.string.remove_current_photo));
        }
        sheet.sheet(R.menu.menu_cancel).listener((dialog, which) -> {
            switch (which) {
                case R.id.sheet_photo_camera:
                    PackageManager pm = getActivity().getPackageManager();
                    if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        checkCameraPermission();
                    } else {
                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                R.string.device_doesnt_have_camera, null);
                    }
                    break;
                case R.id.sheet_photo_gallery:
                    checkGalleryPermission();
                    break;
                case R.id.sheet_photo_remove:
                    removePhoto();
                    break;
                case R.id.cancel:
                    break;
            }
        }).show();
    }

    private void removePhoto() {
        mPhoto.setImageResource(R.drawable.ic_profile_empty);
        ((LoginActivity) getActivity()).setPhotoBitmap(null);
        isPhotoEmpty = true;
    }

    private void checkGalleryPermission() {
        PermissionHelper.requestPermissions(getActivity(),
                LoginActivity.REQUEST_PERMISSION_GALLERY,
                () -> ((LoginActivity) getActivity()).openGallery(), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void checkCameraPermission() {
        PermissionHelper.requestPermissions(getActivity(),
                LoginActivity.REQUEST_PERMISSION_CAMERA,
                () -> ((LoginActivity) getActivity()).openCamera(), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void attachPhoto(String path, boolean createThumbnail) {
        if (createThumbnail) {
            BitmapHelper.createThumbnailFromPhoto(path, this);
        } else {
            BitmapHelper.getThumbnailFromPhoto(getActivity(), path, this);
        }
    }

    @Override
    public void onLoadComplete(String path, Bitmap bitmap) {
        if (CreateProfileFragment.this.isAdded()) {
            ((LoginActivity) getActivity()).setPhotoBitmap(bitmap);
            mPhoto.setImageBitmap(bitmap);
            isPhotoEmpty = false;
        }
    }

    @Override
    public void onError() {
        if (CreateProfileFragment.this.isAdded()) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.error,
                    R.string.file_could_not_be_opened, null);
        }
    }
}