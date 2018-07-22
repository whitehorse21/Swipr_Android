package dk.techtify.swipr.dialog.sell;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.model.sell.Photo;
import dk.techtify.swipr.model.sell.Product;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/10/2017.
 */

public class UploadProductDialog extends BaseDialog {

    private Product mProduct;

    private DoneListener mDoneListener;

    public void setProduct(Product product) {
        this.mProduct = product;
    }

    public void setDoneListener(DoneListener doneListener) {
        this.mDoneListener = doneListener;
    }

    private int mNumber = 0;
    private String mKey;

    private TextView mText;
    private ProgressBar mProgressBar;

    private DatabaseReference mDatabase;

    private List<StorageReference> mFileReferences;
    private List<File> mFiles;
    private ArrayList<String> mServerPaths;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_upload_product, null);

        mText = view.findViewById(R.id.content);
        mProgressBar = view.findViewById(R.id.progress);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mKey = mDatabase.child("product").push().getKey();

        StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl
                (Constants.Firebase.BUCKET_NAME).child("user-product")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(mKey);

        mFileReferences = new ArrayList<>();
        mFiles = new ArrayList<>();
        mServerPaths = new ArrayList<>();
        for (Photo p : mProduct.getLocalPhotos()) {
            File f = new File(p.getLocalPath());
            if (!f.exists()) {
                continue;
            }
            mFileReferences.add(storage.child(f.getName()));
            mFiles.add(f);
        }

        uploadPhoto();

        return view;
    }

    private void uploadPhoto() {
        mText.setText(getString(R.string.compressing_photo) + " " + (mNumber + 1) + " " +
                getString(R.string.from) + " " + mProduct.getLocalPhotos().size());
        mProgressBar.setIndeterminate(true);

        BitmapHelper.createThumbnailFromPhoto(640, mFiles.get(mNumber).getAbsolutePath(),
                new BitmapHelper.LoadCompleteListener() {
                    @Override
                    public void onLoadComplete(String path, Bitmap bitmap) {
                        mText.setText(getString(R.string.uploading_photo) + " " + (mNumber + 1) + " " +
                                getString(R.string.from) + " " + mProduct.getLocalPhotos().size());
                        mProgressBar.setIndeterminate(false);
                        mProgressBar.setProgress(0);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        final byte[] data = baos.toByteArray();

//                        InputStream stream;
//                        try {
//                            stream = new FileInputStream(mFiles.get(mNumber));
//                        } catch (FileNotFoundException e) {
//                            getDialog().dismiss();
//                            return;
//                        }
//                        final long size = mFiles.get(mNumber).length();

                        UploadTask uploadTask = mFileReferences.get(mNumber).putBytes(data);
                        uploadTask.addOnFailureListener(exception -> {
                            getDialog().dismiss();
                            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                    exception.getMessage(), null);
                        }).addOnProgressListener(taskSnapshot -> {
                            final double prg = (100.0 * taskSnapshot.getBytesTransferred()) / data.length;

                            mProgressBar.setProgress((int) prg);
                        }).addOnSuccessListener(taskSnapshot -> {
                            Task<Uri> downloadUrl = mFileReferences.get(mNumber).getDownloadUrl();
                            mServerPaths.add(downloadUrl.getResult().toString().split("\\?")[0]);
                            mNumber += 1;
                            if (mNumber > mFiles.size() - 1) {
                                increaseCounter();
                            } else {
                                uploadPhoto();
                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    private void increaseCounter() {
        mDatabase.child("counter").child(User.getLocalUser().getId()).child("active-posts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long value;
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            value = Long.parseLong(dataSnapshot.getValue().toString()) + 1;
                        } else {
                            value = 1;
                        }

                        mDatabase.child("counter").child(User.getLocalUser().getId()).child("active-posts")
                                .setValue(value).addOnCompleteListener(task -> uploadData());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        uploadData();
                    }
                });
    }

    private void uploadData() {
        mText.setText(getString(R.string.uploading_product_to_server));
        mProgressBar.setIndeterminate(true);

        Map<String, Object> productMap = mProduct.toMap(mKey, mServerPaths);
        Map<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/product/" + mKey, productMap);
        childUpdates.put("/user-product/" + FirebaseAuth.getInstance().getCurrentUser()
                .getUid() + "/" + mKey, productMap);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (UploadProductDialog.this.isAdded()) {
                User.updateContactInfo(mProduct.getContactInfo());
                mDatabase.child("user-data").child(User.getLocalUser().getId()).child("contactInfo")
                        .setValue(mProduct.getContactInfo().toMap());

                if (!task.isSuccessful()) {
                    getDialog().dismiss();
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            task.getException() != null && task.getException()
                                    .getMessage() != null ? task.getException().getMessage() :
                                    getString(R.string.error_unknown), null);
                    return;
                }

                uploadToApi();
            }
        });
    }

    private void uploadToApi() {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                String body;
                try {
                    body = mProduct.toJson(mKey, mServerPaths).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                byte[] data;
                try {
                    data = body.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return false;
                }
                body = Base64.encodeToString(data, Base64.DEFAULT);

                String response;
                try {
                    HttpRequest request = HttpRequest.post(Constants.Url.ADD_PRODUCT).contentType(
                            "application/x-www-form-urlencoded").send("product=" + body);

                    response = request.body();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                if (response == null) {
                    return false;
                }

                if (AppConfig.DEBUG) {
                    Log.d("ADD PRODUCT", "Response: " + response);
                }

                return response.contains("{\"code\":200}");
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (UploadProductDialog.this.isAdded()) {
                    getDialog().dismiss();
                    if (aBoolean) {
                        mDoneListener.onUploadingDone();
                    } else {
                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                getString(R.string.error_unknown), null);
                    }
                }
            }
        }.execute();
    }

    public interface DoneListener {
        void onUploadingDone();
    }
}
