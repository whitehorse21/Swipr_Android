package dk.techtify.swipr.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.fragment.login.CreateProfileFragment;
import dk.techtify.swipr.fragment.login.LoginMainFragment;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.FragmentHelper;
import dk.techtify.swipr.helper.IntentHelper;
import dk.techtify.swipr.helper.OneSignalHelper;
import dk.techtify.swipr.helper.PermissionHelper;
import dk.techtify.swipr.helper.UriHelper;
import dk.techtify.swipr.model.user.User;

public class LoginActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_GALLERY = 1;
    public static final int REQUEST_GALLERY = 2;
    public static final int REQUEST_PERMISSION_CAMERA = 3;
    public static final int REQUEST_CAMERA = 4;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private LoginButton mLoginButton;
    private DatabaseReference mDatabase;

    private Bitmap mPhotoBitmap;

    public void setPhotoBitmap(Bitmap mPhotoBitmap) {
        this.mPhotoBitmap = mPhotoBitmap;
    }

    public LoginButton getLoginButton() {
        return mLoginButton;
    }

    private File mJustCapturedPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            goToMainActivity(false);
            return;
        }

        setContentView(R.layout.activity_login);

        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton = new LoginButton(this);
        mLoginButton.setReadPermissions("email", "public_profile");
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                if (AppConfig.DEBUG) {
                    Log.d("FACEBOOK", "facebook:onSuccess:" + loginResult);
                }

                final AlertDialog progress = DialogHelper.getProgressDialog(LoginActivity.this);
                DialogHelper.showProgressDialog(LoginActivity.this, progress);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    if (AppConfig.DEBUG && object != null) {
                                        Log.d("FACEBOOK", object.toString());
                                    }

                                    if (object == null || !object.has("first_name")
                                            || !object.has("last_name") || !object.has("email")
                                            || object.getString("first_name").length() < 1
                                            || object.getString("last_name").length() < 1
                                            || object.getString("email").length() < 1) {
                                        progress.dismiss();
                                        DialogHelper.showDialogWithCloseAndDone(LoginActivity.this,
                                                R.string.warning, R.string.error_unknown, null);
                                        return;
                                    }

                                    final String firstName = object.getString("first_name");
                                    final String lastName = object.getString("last_name");
                                    final String email = object.getString("email");

                                    if (object.has("picture") && object.getJSONObject("picture").has("data")
                                            && object.getJSONObject("picture").getJSONObject("data").has("url")) {
                                        BitmapHelper.getBitmapFromUrl(object.getJSONObject("picture")
                                                .getJSONObject("data").getString("url"), new BitmapHelper.LoadCompleteListener() {
                                            @Override
                                            public void onLoadComplete(String path, Bitmap bitmap) {
                                                loginWithFacebook(loginResult.getAccessToken(), progress,
                                                        firstName, lastName, email, bitmap);
                                            }

                                            @Override
                                            public void onError() {
                                                loginWithFacebook(loginResult.getAccessToken(), progress,
                                                        firstName, lastName, email, null);
                                            }
                                        });
                                    } else {
                                        loginWithFacebook(loginResult.getAccessToken(), progress,
                                                firstName, lastName, email, null);
                                    }
                                } catch (JSONException e) {
                                    progress.dismiss();
                                    DialogHelper.showDialogWithCloseAndDone(LoginActivity.this,
                                            R.string.warning, R.string.error_unknown, null);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name,last_name,email,picture.width(240)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                if (AppConfig.DEBUG) {
                    Log.d("FACEBOOK", "facebook:onCancel");
                }
            }

            @Override
            public void onError(FacebookException error) {
                if (AppConfig.DEBUG) {
                    Log.d("FACEBOOK", "facebook:onError", error);
                }
                DialogHelper.showDialogWithCloseAndDone(LoginActivity.this,
                        R.string.warning, error.getMessage() != null ? error.getMessage() :
                                getString(R.string.error_unknown), null);
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new LoginMainFragment()).commit();

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        if (googleApiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(this);
        }
    }

    private void loginWithFacebook(AccessToken token, final AlertDialog progress, final String firstName, final String lastName, final String email, final Bitmap photo) {
        if (AppConfig.DEBUG) {
            Log.d("FACEBOOK", "handleFacebookAccessToken:" + token);
        }

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (AppConfig.DEBUG) {
                    Log.d("FIREBASE AUTH", "signInWithCredential:onComplete:" + task.isSuccessful());
                }
                LoginManager.getInstance().logOut();

                if (!task.isSuccessful()) {
                    if (AppConfig.DEBUG) {
                        Log.w("FIREBASE AUTH", "signInWithCredential", task.getException());
                    }
                    progress.dismiss();
                    DialogHelper.showDialogWithCloseAndDone(LoginActivity.this,
                            R.string.warning, task.getException() != null && task.getException()
                                    .getMessage() != null ? task.getException().getMessage() :
                                    getString(R.string.error_unknown), null);
                    return;
                }

                mDatabase.child("user-data").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                            // should add facebook user to database
                            uploadPhotoBeforeUserCreation(progress, firstName, lastName, email, photo, 0);
                        } else {
                            uploadFacebookPhotoBeforeLogin(progress, photo);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                        DialogHelper.showDialogWithCloseAndDone(LoginActivity.this, R.string.warning,
                                databaseError.getMessage() != null ? databaseError.getMessage() :
                                        getString(R.string.error_unknown), null);
                    }
                });
            }
        });
    }

    public void loginWithEmailAndPassword(final String email, final String password) {
        final AlertDialog progress = DialogHelper.getProgressDialog(this);
        DialogHelper.showProgressDialog(this, progress);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (AppConfig.DEBUG) {
                            Log.d("FIREBASE AUTH", "signInWithEmail:onComplete:" + task.isSuccessful());
                        }

                        if (!task.isSuccessful()) {
                            progress.dismiss();
                            DialogHelper.showDialogWithCloseAndDone(LoginActivity.this, R.string.warning,
                                    task.getException() != null && task.getException()
                                            .getMessage() != null ? task.getException().getMessage() :
                                            getString(R.string.error_unknown), null);
                            return;
                        }

                        getUserFromDatabase(progress);
                    }
                });
    }

    public void registrationWithEmailAndPassword(final String firstName, final String lastName, final String email, String password, final int gender) {
        final AlertDialog progress = DialogHelper.getProgressDialog(this);
        DialogHelper.showProgressDialog(LoginActivity.this, progress);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (AppConfig.DEBUG) {
                            Log.d("FIREBASE AUTH", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        }

                        if (!task.isSuccessful()) {
                            progress.dismiss();
                            DialogHelper.showDialogWithCloseAndDone(LoginActivity.this, R.string.warning,
                                    task.getException() != null && task.getException()
                                            .getMessage() != null ? task.getException().getMessage() :
                                            getString(R.string.error_unknown), null);
                            return;
                        }

                        uploadPhotoBeforeUserCreation(progress, firstName, lastName, email, mPhotoBitmap, gender);
                    }
                });
    }

    private void uploadFacebookPhotoBeforeLogin(final AlertDialog progress, Bitmap photo) {
        if (photo == null) {
            getUserFromDatabase(progress);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.Firebase.BUCKET_NAME);
        final StorageReference mountainsRef = storageRef.child("user-data/" + mAuth.getCurrentUser().getUid() + "/profilePicture.png");

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                getUserFromDatabase(progress);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDatabase.child("user-data").child(mAuth.getCurrentUser().getUid()).child("photoUrl")
                        .setValue(mountainsRef.getDownloadUrl().getResult().toString().split("\\?")[0])
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                getUserFromDatabase(progress);
                            }
                        });
            }
        });
    }

    private void uploadPhotoBeforeUserCreation(final AlertDialog progress, final String firstName, final String lastName, final String email, Bitmap photo, final int gender) {
        if (photo == null) {
            createUser(progress, firstName, lastName, email, null, gender);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.Firebase.BUCKET_NAME);
        final StorageReference mountainsRef = storageRef.child("user-data/" + mAuth.getCurrentUser().getUid() + "/profilePicture.png");

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                createUser(progress, firstName, lastName, email, null, gender);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                createUser(progress, firstName, lastName, email, mountainsRef.getDownloadUrl().getResult().toString().split("\\?")[0], gender);
            }
        });
    }

    private void createUser(final AlertDialog progress, String firstName, String lastName, String email, String photoUrl, int gender) {
        mDatabase.child("user-data").child(mAuth.getCurrentUser().getUid()).setValue(User
                .mapOfNewUser(firstName, lastName, email, photoUrl, gender))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (AppConfig.DEBUG) {
                            Log.d("FIREBASE AUTH", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        }

                        if (!task.isSuccessful()) {
                            progress.dismiss();
                            DialogHelper.showDialogWithCloseAndDone(LoginActivity.this, R.string.warning,
                                    task.getException() != null && task.getException()
                                            .getMessage() != null ? task.getException().getMessage() :
                                            getString(R.string.error_unknown), null);
                            return;
                        }

                        getUserFromDatabase(progress);
                    }
                });
    }

    public void anonymousLogin() {
        final AlertDialog progress = DialogHelper.getProgressDialog(LoginActivity.this);
        DialogHelper.showProgressDialog(LoginActivity.this, progress);
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (AppConfig.DEBUG) {
                            Log.d("FIREBASE AUTH", "signInAnonymously:onComplete:" + task.isSuccessful());
                        }

                        progress.dismiss();

                        if (!task.isSuccessful()) {
                            if (AppConfig.DEBUG) {
                                Log.w("FIREBASE AUTH", "signInAnonymously", task.getException());
                            }
                            DialogHelper.showDialogWithCloseAndDone(LoginActivity.this,
                                    R.string.warning, task.getException() != null && task.getException()
                                            .getMessage() != null ? task.getException().getMessage() :
                                            getString(R.string.error_unknown), null);
                            return;
                        }

                        getUserFromDatabase(progress);
                    }
                });
    }

    private void getUserFromDatabase(final AlertDialog progress) {
        if (mAuth.getCurrentUser().isAnonymous()) {
            goToMainActivity(true);
            return;
        }
        mDatabase.child("user-data").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progress.dismiss();
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    DialogHelper.showDialogWithCloseAndDone(LoginActivity.this, R.string.warning,
                            getString(R.string.error_unknown), null);
                    return;
                }

                mDatabase.child("user-data").child(mAuth.getCurrentUser().getUid()).child("locale").setValue(Locale.getDefault().getLanguage());

                User.saveLocalUser(mAuth.getCurrentUser().getUid(), (HashMap<String, Object>) dataSnapshot.getValue());

                OneSignal.syncHashedEmail(User.getLocalUser().getEmail());
                OneSignalHelper.subscribe();

                goToMainActivity(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.dismiss();
            }
        });
    }

    private void goToMainActivity(boolean newTask) {
        Intent intent = new Intent(this, MainActivity.class);
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
        if (!newTask) {
            this.finish();
        }
    }

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                if (AppConfig.DEBUG) {
                    Log.d("FIREBASE AUTH", "onAuthStateChanged:signed_in:" + user.getUid());
                }
            } else {
                if (AppConfig.DEBUG) {
                    Log.d("FIREBASE AUTH", "onAuthStateChanged:signed_out");
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, LoginActivity.REQUEST_GALLERY);
        } catch (ActivityNotFoundException e) {
            DialogHelper.showDialogWithCloseAndDone(this, R.string.warning,
                    R.string.couldnot_find_apps_for_this_action, null);
        }
    }

    public void openCamera() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            mJustCapturedPhotoFile = UriHelper.createImageFile();

            if (mJustCapturedPhotoFile == null) {
                DialogHelper.showDialogWithCloseAndDone(this, R.string.error, R.string.photo_could_not_be_taken, null);
                return;
            }

            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", mJustCapturedPhotoFile));
            startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean allPermissionsGranted = PermissionHelper.allPermissionsGranted(grantResults);
        if (requestCode == REQUEST_PERMISSION_GALLERY) {
            if (allPermissionsGranted) {
                openGallery();
            } else {
                DialogHelper.showDialogWithCloseAndDone(this, R.string.warning,
                        R.string.please_grant_us_a_permission_gallery,
                        new DialogHelper.OnActionListener() {
                            @Override
                            public void onPositive(Object o) {
                                IntentHelper.openAppSettings(LoginActivity.this);
                            }
                        });
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (allPermissionsGranted) {
                openCamera();
            } else {
                DialogHelper.showDialogWithCloseAndDone(this, R.string.warning,
                        R.string.please_grant_us_a_permission_camera,
                        new DialogHelper.OnActionListener() {
                            @Override
                            public void onPositive(Object o) {
                                IntentHelper.openAppSettings(LoginActivity.this);
                            }
                        });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY) {
            if (resultCode != RESULT_OK) {
                return;
            }
            Uri uri = data.getData();
            retrievePathFromUri(uri, false);
        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode != RESULT_OK || mJustCapturedPhotoFile == null || !mJustCapturedPhotoFile.exists()) {
                DialogHelper.showDialogWithCloseAndDone(this, R.string.error, R.string.photo_could_not_be_taken, null);
                return;
            }

            final Uri uri = Uri.fromFile(mJustCapturedPhotoFile);

            retrievePathFromUri(uri, true);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void retrievePathFromUri(Uri uri, boolean createThumbnail) {
        String path;
        if (Build.VERSION.SDK_INT < 19) {
            if (uri.toString().contains("content://com.google.android.apps.photos.content")) {
                path = UriHelper.getPath(this, uri);
            } else {
                path = UriHelper.getPathFromContentUri(this, uri);
            }
        } else {
            path = UriHelper.getPath(this, uri);
        }

        if (AppConfig.DEBUG) {
            Log.d("FILE FROM GALLERY", "path=" + path);
        }

        if (path == null) {
            DialogHelper.showDialogWithCloseAndDone(this, R.string.error, R.string.file_could_not_be_opened, null);
            return;
        }

        if (FragmentHelper.getCurrentFragment(this) instanceof CreateProfileFragment) {
            ((CreateProfileFragment) FragmentHelper.getCurrentFragment(this)).attachPhoto(path, createThumbnail);
        }
    }
}