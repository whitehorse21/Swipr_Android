package dk.techtify.swipr.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.wunderlist.slidinglayer.SlidingLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.SwiprApp;
import dk.techtify.swipr.activity.sell.AddPhotoActivity;
import dk.techtify.swipr.billing.IabHelper;
import dk.techtify.swipr.billing.IabResult;
import dk.techtify.swipr.billing.Inventory;
import dk.techtify.swipr.billing.Purchase;
import dk.techtify.swipr.dialog.main.SwiprPlusDialog;
import dk.techtify.swipr.dialog.sell.AddPhotoDialog;
import dk.techtify.swipr.fragment.main.AboutFragment;
import dk.techtify.swipr.fragment.main.FaqFragment;
import dk.techtify.swipr.fragment.main.FavouritesFragment;
import dk.techtify.swipr.fragment.main.MessagesFragment;
import dk.techtify.swipr.fragment.main.MyProfileFragment;
import dk.techtify.swipr.fragment.main.SellFragment;
import dk.techtify.swipr.fragment.main.StoreFragment;
import dk.techtify.swipr.fragment.sell.SellAdditionalInfoFragment;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.FragmentHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.helper.PermissionHelper;
import dk.techtify.swipr.helper.PhotoHelper;
import dk.techtify.swipr.helper.SpHelper;
import dk.techtify.swipr.model.sell.Photo;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.push.SwiprNotificationExtender;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 12/10/2016.
 */
public class MainActivity extends BaseActivity implements SwiprPlusDialog.DealListener, IabHelper.OnIabPurchaseFinishedListener {

    public static final int REQUEST_PLAYER_RECOVERY = 1;
    public static final int REQUEST_SELL_ADD_PHOTO = 2;
    public static final int REQUEST_PURCHASE = 3;

    private SlidingLayer mMenuLayer;
    private ActionView mActionView;
    private SlidingMenu mSlidingMenu;
    private FrameLayout mContainer;

    public SlidingLayer getMenuLayer() {
        return mMenuLayer;
    }

    public ActionView getActionView() {
        return mActionView;
    }

    public SlidingMenu getPlusSlidingBar() {
        return mSlidingMenu;
    }

    private File mJustCapturedPhotoFile;

    public File getJustCapturedPhotoFile() {
        return mJustCapturedPhotoFile;
    }

    public void setJustCapturedPhotoFile(File mJustCapturedPhotoFile) {
        this.mJustCapturedPhotoFile = mJustCapturedPhotoFile;
    }

    private IabHelper mIabHelper;
    private boolean isIabHelperWorking;

    public boolean isIabHelperWorking() {
        return mIabHelper != null && isIabHelperWorking;
    }

    PhotoHelper.ExecuteListener mGetPhotoExecuteListener = (path, fromGallery) -> {
        AddPhotoDialog addPhotoDialog = (AddPhotoDialog) getSupportFragmentManager()
                .findFragmentByTag(AddPhotoDialog.class.getSimpleName());
        if (addPhotoDialog != null) {
            addPhotoDialog.attachPhoto(path, fromGallery);
        }
    };
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = (purchase, result) -> {
        if (AppConfig.DEBUG) {
            Log.d("IN-APP PURCHASE", purchase.getSku() + " renewed: " + result.isSuccess());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwiprNotificationExtender.clearNotifications(this);

        SpHelper.checkViewPreferences();

        if (Build.VERSION.SDK_INT > 20) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(DisplayHelper.isScreenSmall() ? R.layout.activity_main_small : R.layout.activity_main);
        initSideMenu();
        if (DisplayHelper.hasNavigationBar(this)) {
            FrameLayout.LayoutParams rootParams = (FrameLayout.LayoutParams) findViewById(R.id.root).getLayoutParams();
            rootParams.bottomMargin = DisplayHelper.getNavigationBarHeight(this, DisplayHelper.getScreenOrientation(this));
        }

        mContainer = findViewById(R.id.container);
        mActionView = findViewById(R.id.action_view);
        if (Build.VERSION.SDK_INT > 20) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mActionView.getLayoutParams();
            params.topMargin = DisplayHelper.getStatusBarHeight(this);
            findViewById(R.id.menu_content).setPadding(0, DisplayHelper.getStatusBarHeight(this), 0, 0);
        }
        mActionView.setMenuClickListener(() -> {
            if (mMenuLayer.isOpened()) {
                mMenuLayer.closeLayer(true);
            } else if (mMenuLayer.isClosed()) {
                mMenuLayer.setVisibility(View.VISIBLE);
                mMenuLayer.post(() -> {
                    IoHelper.hideKeyboard(MainActivity.this, mMenuLayer);
                    mMenuLayer.openLayer(true);
                });
            }
        });

        mMenuLayer = findViewById(R.id.menu_layer);
        setupMenuLayer();

        checkContentHeight();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new StoreFragment())
                    .commitAllowingStateLoss();
        }

        if (User.getLocalUser().getEmail() != null) {
            if (NetworkHelper.isOnline(this, NetworkHelper.NONE)) {
                User.refreshLocalUser();
                connection();
            }

            mIabHelper = SwiprApp.getInstance().getIabHelper();
            mIabHelper.enableDebugLogging(AppConfig.DEBUG);
            setupIabHelper();
        }
    }

    private void openMyProfile() {
        if (!User.checkSignIn(MainActivity.this)) {
            return;
        }

        if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof MyProfileFragment)) {
            FragmentHelper.replaceFragment(MainActivity.this, new MyProfileFragment());
        }
        mMenuLayer.closeLayer(true);
    }

    @Override
    public void onBackPressed() {
        if (mMenuLayer.isOpened()) {
            mMenuLayer.closeLayer(true);
            return;
        }
        if (FragmentHelper.getCurrentFragment(this) != null && FragmentHelper.getCurrentFragment(this)
                .getChildFragmentManager().getBackStackEntryCount() > 0) {
            FragmentHelper.getCurrentFragment(this).getChildFragmentManager().popBackStack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLAYER_RECOVERY) {
            if (FragmentHelper.getCurrentFragment(this) instanceof AboutFragment) {
                ((AboutFragment) FragmentHelper.getCurrentFragment(this)).initializePlayer();
            }
        } else if (requestCode == PhotoHelper.REQUEST_GALLERY) {
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
        } else if (requestCode == REQUEST_SELL_ADD_PHOTO) {
            if (resultCode == RESULT_OK && FragmentHelper.getCurrentFragment(this) instanceof SellFragment) {
                ((SellFragment) FragmentHelper.getCurrentFragment(this)).onPhotoSelected(
                        (ArrayList<Photo>) data.getSerializableExtra(AddPhotoActivity.EXTRA_PHOTO_LIST));
            }
        } else if (requestCode == REQUEST_PURCHASE) {
            if (resultCode == RESULT_OK) {
                if (mIabHelper == null) {
                    return;
                }

                if (AppConfig.DEBUG) {
                    Log.d("IN-APP PURCHASE", "onActivityResult handled by IABUtil. Operation finished");
                }
                mIabHelper.handleActivityResult(requestCode, resultCode, data);
            } else {
                if (AppConfig.DEBUG) {
                    Log.d("IN-APP PURCHASE", "onActivityResult handled by IABUtil. Operation canceled");
                }
            }
        }
//        else if (requestCode == REQUEST_NEW_MESSAGE_FOLLOWER) {
//            if (resultCode == RESULT_OK && FragmentHelper.getCurrentFragment(this) instanceof MessagesFragment) {
//                ((MessagesFragment) FragmentHelper.getCurrentFragment(this)).startChatWithFollower(
//                        (Follow) data.getSerializableExtra(FollowersActivity.EXTRA_FOLLOWER));
//            }
//        }
    }

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

    private void setupIabHelper() {
        try {
            mIabHelper.startSetup(result -> {
                if (AppConfig.DEBUG) {
                    Log.d("IN-APP PURCHASE", "IabHelper setup finished.");
                }
                if (!result.isSuccess()) {
                    if (AppConfig.DEBUG) {
                        Log.d("IN-APP PURCHASE", "Problem setting up In-app Billing: " + result);
                    }
                    return;
                }
                if (mIabHelper == null) {
                    return;
                }
                if (AppConfig.DEBUG) {
                    Log.d("IN-APP PURCHASE", "Setup successful. Querying inventory.");
                }

                mIabHelper.queryInventoryAsync(true, Arrays.asList(Constants.Purchase.ID_LIST),
                        mSkuListQueryFinishedListener);
            });
        } catch (IllegalStateException e) {
            if (AppConfig.DEBUG) {
                e.printStackTrace();
            }
            if (mIabHelper != null) {
                mIabHelper.flagEndAsync();
                mIabHelper.queryInventoryAsync(true, Arrays.asList(Constants.Purchase.ID_LIST),
                        mSkuListQueryFinishedListener);
            }
        } catch (Exception e) {
            if (AppConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void setupMenuLayer() {
        findViewById(R.id.store).setOnClickListener(view -> {
            if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof StoreFragment)) {
                Fragment previous = getSupportFragmentManager().findFragmentByTag(StoreFragment
                        .class.getName());
                FragmentHelper.replaceFragment(MainActivity.this, previous == null
                        ? new StoreFragment() : previous);
            }
            mMenuLayer.closeLayer(true);
        });
        findViewById(R.id.sell).setOnClickListener(view -> {
            if (!User.checkSignIn(MainActivity.this)) {
                return;
            }

            if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof SellFragment)) {
                FragmentHelper.replaceFragment(MainActivity.this, new SellFragment());
            }
            mMenuLayer.closeLayer(true);
        });
        findViewById(R.id.message).setOnClickListener(view -> {
            if (!User.checkSignIn(MainActivity.this)) {
                return;
            }

            if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof MessagesFragment)) {
                FragmentHelper.replaceFragment(MainActivity.this, new MessagesFragment());
            }
            mMenuLayer.closeLayer(true);
        });
        findViewById(R.id.favourites).setOnClickListener(view -> {
            if (!User.checkSignIn(MainActivity.this)) {
                return;
            }

            if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof FavouritesFragment)) {
                FragmentHelper.replaceFragment(MainActivity.this, new FavouritesFragment());
            }
            mMenuLayer.closeLayer(true);
        });
        findViewById(R.id.faq).setOnClickListener(view -> {
            if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof FaqFragment)) {
                FragmentHelper.replaceFragment(MainActivity.this, new FaqFragment());
            }
            mMenuLayer.closeLayer(true);
        });
        findViewById(R.id.about).setOnClickListener(view -> {
            if (!(FragmentHelper.getCurrentFragment(MainActivity.this) instanceof AboutFragment)) {
                FragmentHelper.replaceFragment(MainActivity.this, new AboutFragment());
            }
            mMenuLayer.closeLayer(true);
        });
        findViewById(R.id.menu_menu).setOnClickListener(view -> mMenuLayer.closeLayer(true));
        findViewById(R.id.menu_settings).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        TextView name = findViewById(R.id.menu_name);
        name.setText(User.getLocalUser().getName());
        name.setOnClickListener(v -> {

        });
        name.setOnClickListener(v -> openMyProfile());

        mMenuLayer.setSlidingEnabled(false);
        mMenuLayer.setOnInteractListener(new SlidingLayer.OnInteractListener() {
            @Override
            public void onOpen() {
                mMenuLayer.setSlidingEnabled(true);
            }

            @Override
            public void onShowPreview() {

            }

            @Override
            public void onClose() {
                mMenuLayer.setSlidingEnabled(false);
            }

            @Override
            public void onOpened() {

            }

            @Override
            public void onPreviewShowed() {

            }

            @Override
            public void onClosed() {
                mMenuLayer.setVisibility(View.GONE);
            }
        });
        if (User.getLocalUser().getEmail() != null) {
            ImageView photo = findViewById(R.id.menu_user_photo);
            if (!TextUtils.isEmpty(User.getLocalUser().getPhotoUrl())) {
                GlideApp.with(this)
                        .load(FirebaseStorage.getInstance().getReferenceFromUrl(User.getLocalUser().getPhotoUrl()))
                        .into(photo);
            }
            photo.setOnClickListener(v -> openMyProfile());
        } else {
            findViewById(R.id.menu_user_photo).setVisibility(View.INVISIBLE);
        }
    }

    public void enableSideMenu(boolean enable) {
        mSlidingMenu.setSlidingEnabled(enable);
    }

    private void checkContentHeight() {
        if (!SwiprApp.getInstance().getSp().contains(Constants.Prefs.CONTENT_HEIGHT)) {
            findViewById(R.id.container).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    findViewById(R.id.container).removeOnLayoutChangeListener(this);

                    SwiprApp.getInstance().getSp().edit().putInt(Constants.Prefs.CONTENT_HEIGHT,
                            bottom - top).apply();
                }
            });
        }
    }

    public void setContainerTopMargin(boolean add) {
        if (Build.VERSION.SDK_INT > 20) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContainer.getLayoutParams();
            if (add) {
                params.topMargin = DisplayHelper.getStatusBarHeight(this);
            } else {
                params.topMargin = 0;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void goOffline() {
        final DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference(
                "user-data/" + User.getLocalUser().getId() + "/connection/online");
        final DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference(
                "user-data/" + User.getLocalUser().getId() + "/connection/chat-room");
        onlineRef.removeValue();
        chatsRef.removeValue();
    }

    private void connection() {
        final DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference(
                "user-data/" + User.getLocalUser().getId() + "/connection/online");
        final DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference(
                "user-data/" + User.getLocalUser().getId() + "/connection/chat-room");

        final DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(
                ".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    onlineRef.setValue(Boolean.TRUE);

                    onlineRef.onDisconnect().removeValue();
                    chatsRef.onDisconnect().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        if (AppConfig.DEBUG) {
            Log.d("IN-APP PURCHASE", "Purchase finished: " + result + ", purchase: " + info);
        }

        if ((SwiprApp.getInstance().getIabHelper()) == null) {
            Log.d("IN-APP PURCHASE", "Purchase unsuccessful. Iab is null.");
            return;
        }

        if (result.isFailure()) {
            Log.d("IN-APP PURCHASE", "Purchase unsuccessful. Result is failed.");
            return;
        }

        if (AppConfig.DEBUG) {
            Log.d("IN-APP PURCHASE", "Purchase successful.");
        }

        if (info.getSku().equals(Constants.Purchase.SWIPR_PLUS_MEMBERSHIP)) {
            FirebaseDatabase.getInstance().getReference("user-data/" + User.getLocalUser().getId() +
                    "/plusSubscriptionAndroidId").setValue(info.getOrderId());
            User.getLocalUser().setPlusSubscriptionId(info.getOrderId());
        }

        try {
            mIabHelper.queryInventoryAsync(true, Arrays.asList(Constants.Purchase.ID_LIST),
                    mSkuListQueryFinishedListener);
        } catch (IllegalStateException e) {
            if (AppConfig.DEBUG) {
                e.printStackTrace();
            }
            if (mIabHelper != null) {
                mIabHelper.flagEndAsync();
                mIabHelper.queryInventoryAsync(true, Arrays.asList(Constants.Purchase.ID_LIST),
                        mSkuListQueryFinishedListener);
            }
        } catch (Exception e) {
            if (AppConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        if (FragmentHelper.getCurrentFragment(this) instanceof SellAdditionalInfoFragment) {
            ((SellAdditionalInfoFragment) FragmentHelper.getCurrentFragment(this))
                    .oneTimePurchaseSuccessful();
        }
    }

    IabHelper.QueryInventoryFinishedListener mSkuListQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                if (AppConfig.DEBUG) {
                    Log.d("IN-APP PURCHASE", "Requesting sku list failure.");
                }
                return;
            }

            if (AppConfig.DEBUG) {
                Log.d("IN-APP PURCHASE", "Requesting sku list successful.");
            }

            isIabHelperWorking = true;

            Purchase oneTimePurchase = inventory.getPurchase(Constants.Purchase.ONE_TIME_PRODUCT_BOOSTER);
            if (oneTimePurchase != null) {
                mIabHelper.consumeAsync(oneTimePurchase, mConsumeFinishedListener);
            }

            Purchase swiprPlusPurchase = inventory.getPurchase(Constants.Purchase.SWIPR_PLUS_MEMBERSHIP);
            if (swiprPlusPurchase != null && User.getLocalUser().getPlusSubscriptionAndroidId() != null
                    && swiprPlusPurchase.getOrderId().equals(User.getLocalUser().getPlusSubscriptionAndroidId())) {
                FirebaseDatabase.getInstance().getReference("user-data/" + User.getLocalUser().getId() + "/isPlusMember").setValue(true);
                User.getLocalUser().setPlusMember(true);
                if (FragmentHelper.getCurrentFragment(MainActivity.this) instanceof StoreFragment) {
                    ((StoreFragment) FragmentHelper.getCurrentFragment(MainActivity.this)).setPlusMemberVisibility(true);
                } else if (FragmentHelper.getCurrentFragment(MainActivity.this) instanceof SellAdditionalInfoFragment) {
                    ((SellAdditionalInfoFragment) FragmentHelper.getCurrentFragment(MainActivity.this)).plusSubscriptionSuccessful();
                }
            } else {
                FirebaseDatabase.getInstance().getReference("user-data/" + User.getLocalUser().getId() + "/isPlusMember").setValue(false);
//                FirebaseDatabase.getInstance().getReference("user-data/" + User.getLocalUser().getId() + "/plusSubscriptionAndroidId").removeValue();
                User.getLocalUser().setPlusMember(false);
                if (FragmentHelper.getCurrentFragment(MainActivity.this) instanceof StoreFragment) {
                    ((StoreFragment) FragmentHelper.getCurrentFragment(MainActivity.this)).setPlusMemberVisibility(false);
                }
            }
        }
    };

    private void initSideMenu() {
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setFadeEnabled(false);
        View view = getLayoutInflater().inflate(R.layout.plus_side_menu_alert, null);
        view.setOnClickListener(v -> {
            mSlidingMenu.toggle(true);

            SwiprPlusDialog spd = new SwiprPlusDialog();
            spd.setDealListener(MainActivity.this);
            spd.show(getSupportFragmentManager(), spd.getClass().getSimpleName());
        });
        mSlidingMenu.setMenu(view);
        mSlidingMenu.setBehindWidth(DisplayHelper.dpToPx(this, 100));
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setSlidingEnabled(false);
        mSlidingMenu.setBehindScrollScale(1);
    }

    @Override
    public void swiprPlusDeal() {
        if (User.checkSignIn(this) && NetworkHelper.isOnline(this, NetworkHelper.ALERT)) {
            openPurchaseDialog(Constants.Purchase.SWIPR_PLUS_MEMBERSHIP);
        }
    }

    public void openPurchaseDialog(String id) {
        if (AppConfig.DEBUG) {
            Log.d("IN-APP PURCHASE", "ID " + id + ". Purchase button clicked; launching purchase flow for upgrade.");
        }

        if (isIabHelperWorking())
            try {
                SwiprApp.getInstance().getIabHelper().launchPurchaseFlow(this, id,
                        MainActivity.REQUEST_PURCHASE, this, Constants.Purchase.PAYLOAD);
            } catch (IllegalStateException e) {
                if (AppConfig.DEBUG) {
                    e.printStackTrace();
                }
                SwiprApp.getInstance().getIabHelper().flagEndAsync();
                SwiprApp.getInstance().getIabHelper().launchPurchaseFlow(this, id,
                        MainActivity.REQUEST_PURCHASE, this, Constants.Purchase.PAYLOAD);
            } catch (Exception e) {
                if (AppConfig.DEBUG) {
                    e.printStackTrace();
                }
                DialogHelper.showDialogWithCloseAndDone(this, R.string.warning, R.string.error_purchase, null);
            }
        else {
            DialogHelper.showDialogWithCloseAndDone(this, R.string.warning, R.string.error_purchase, null);
        }
    }
}
