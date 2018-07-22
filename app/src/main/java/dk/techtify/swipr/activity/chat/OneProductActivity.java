package dk.techtify.swipr.activity.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.SwiprApp;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.activity.store.ProductDetailsActivity;
import dk.techtify.swipr.dialog.store.BidMessageDialog;
import dk.techtify.swipr.dialog.store.BidOverviewDialog;
import dk.techtify.swipr.dialog.store.BidPriceDialog;
import dk.techtify.swipr.dialog.store.BidSuccessfulDialog;
import dk.techtify.swipr.dialog.store.BidTimerDialog;
import dk.techtify.swipr.dialog.store.SellerBuyerDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.store.OutgoingBid;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.RatingBar;

/**
 * Created by Pavel on 1/26/2017.
 */

public class OneProductActivity extends BaseActivity {

    public static final String EXTRA_SELLER_ID = "dk.techtify.swipr.activity.chat.OneProductActivity.EXTRA_SELLER_ID";
    public static final String EXTRA_ID = "dk.techtify.swipr.activity.chat.OneProductActivity.EXTRA_ID";
    public static final String EXTRA_PHOTO_URL = "dk.techtify.swipr.activity.chat.OneProductActivity.EXTRA_PHOTO_URL";

    private DatabaseReference mDatabase;
    private Product mProduct;
    private SellerBuyer mSeller;
    private OutgoingBid mOutgoingBid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_one_product);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getIntent().hasExtra(EXTRA_PHOTO_URL)) {
            GlideApp.with(this)
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(
                            getIntent().getStringExtra(EXTRA_PHOTO_URL)))
                    .into((ImageView) findViewById(R.id.photo));
        }

        if (getIntent().getStringExtra(EXTRA_SELLER_ID).equals(User.getLocalUser().getId())) {
            findViewById(R.id.bid).setVisibility(View.GONE);
        }

        mDatabase.child("user-product").child(getIntent().getStringExtra(EXTRA_SELLER_ID)).child(
                getIntent().getStringExtra(EXTRA_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("RedundantStringToString")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    mProduct = new Product(dataSnapshot.getKey().toString(), (Map<String, Object>) dataSnapshot.getValue());
                }

                setProductFields();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("user-data").child(getIntent().getStringExtra(EXTRA_SELLER_ID))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressWarnings({"RedundantStringToString", "unchecked"})
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            mSeller = new SellerBuyer(dataSnapshot.getKey().toString(),
                                    (Map<String, Object>) dataSnapshot.getValue());
                        }

                        setSellerFields();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setProductFields() {
        if (!getIntent().hasExtra(EXTRA_PHOTO_URL) && !TextUtils.isEmpty(mProduct.getPhotos().get(0))) {
            GlideApp.with(this)
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(
                            mProduct.getPhotos().get(0)))
                    .into((ImageView) findViewById(R.id.photo));
        }
        if (mProduct.getSize() != null && !mProduct.getSize().isEmpty()) {
            ((TextView) findViewById(R.id.size)).setText(mProduct.getSize());
        } else {
            findViewById(R.id.size).setVisibility(View.INVISIBLE);
        }
        ((TextView) findViewById(R.id.name)).setText(mProduct.getName());
        ((TextView) findViewById(R.id.price)).setText(TextUtils.concat(String.valueOf(mProduct.getPrice()), " ", getString(R.string.kr)));
        ((TextView) findViewById(R.id.location)).setText(mProduct.getContactInfo().getCity());
        findViewById(R.id.photo).setOnClickListener(v -> {
            Intent intent = new Intent(OneProductActivity.this, ProductDetailsActivity.class);
            intent.putExtra(ProductDetailsActivity.EXTRA_TITLE, mProduct.getName());
            intent.putExtra(ProductDetailsActivity.EXTRA_DESCRIPTION, mProduct.getDescription());
            intent.putExtra(ProductDetailsActivity.EXTRA_PHOTOS, mProduct.getPhotos());
            OneProductActivity.this.startActivity(intent);
        });

        if (mProduct.getStatus() > 0) {
            findViewById(R.id.bid).setVisibility(View.GONE);
        }
        findViewById(R.id.bid).setOnClickListener(v -> {
            if (BaseActivity.getOutgoingBids().contains(mProduct.getId())) {
                DialogHelper.showDialogWithCloseAndDone(OneProductActivity.this, R.string.warning,
                        R.string.you_have_active_bid, null);
                return;
            }
            if (mSeller != null) {
                placeBid(true, false);
            }
        });
    }

    private void setSellerFields() {
        if (!TextUtils.isEmpty(mSeller.getPhotoUrl())) {
            GlideApp.with(this)
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(
                            mSeller.getPhotoUrl()))
                    .into((ImageView) findViewById(R.id.seller_photo));
        }
        findViewById(R.id.plus_member).setVisibility(mSeller.isPlusMember() ? View.VISIBLE : View.INVISIBLE);
        ((TextView) findViewById(R.id.seller_name)).setText(mSeller.getName());
        findViewById(R.id.seller_photo).setOnClickListener(v -> {
            SellerBuyerDialog uid = SellerBuyerDialog.newInstance(mSeller);
            uid.show(getSupportFragmentManager(), uid.getClass().getSimpleName());
        });
    }

    private void placeBid(boolean newBid, boolean reverse) {
        if (newBid) {
            mOutgoingBid = new OutgoingBid();

            mOutgoingBid.setBidderId(User.getLocalUser().getId());
            mOutgoingBid.setProductId(mProduct.getId());
            mOutgoingBid.setSellerId(mProduct.getUserId());
            mOutgoingBid.setPhotoUrl(mProduct.getPhotos().get(0));
            mOutgoingBid.setBrand(mProduct.getBrand());
            mOutgoingBid.setType(mProduct.getType());
            mOutgoingBid.setInitialPrice(mProduct.getPrice());
        }

        final BidPriceDialog bidPriceDialog = BidPriceDialog.newInstance(reverse);
        bidPriceDialog.setBid(mOutgoingBid);
        bidPriceDialog.setPriceListener(new BidPriceDialog.ActionListener() {
            @Override
            public void onPriceSet(int price) {
                mOutgoingBid.setPrice(price);

                BidOverviewDialog bidOverviewDialog = new BidOverviewDialog();
                bidOverviewDialog.setBid(mOutgoingBid);
                bidOverviewDialog.setDoneListener(new BidOverviewDialog.DoneListener() {
                    @Override
                    public void onDone() {
                        if (NetworkHelper.isOnline(OneProductActivity.this, NetworkHelper.ALERT)) {
                            sendBid();
                        }
                    }

                    @Override
                    public void onBack() {
                        placeBid(false, true);
                    }
                });
                bidOverviewDialog.show(getSupportFragmentManager(), bidOverviewDialog
                        .getClass().getSimpleName());
            }

            @Override
            public void onTimerClick() {
                BidTimerDialog bidTimerDialog = BidTimerDialog.newInstance(mOutgoingBid.getTimerHrsPosition(), mOutgoingBid
                        .getTimerMinPosition());
                bidTimerDialog.setSizeSelectedListener(new BidTimerDialog.TimeSelectedListener() {
                    @Override
                    public void onTimeSelected(int hourPosition, int minPosition) {
                        mOutgoingBid.setTimerHrsPosition(hourPosition);
                        mOutgoingBid.setTimerMinPosition(minPosition);
                    }

                    @Override
                    public void onBack() {
                        placeBid(false, true);
                    }
                });
                bidTimerDialog.show(getSupportFragmentManager(), bidTimerDialog
                        .getClass().getSimpleName());
            }

            @Override
            public void onMessageClick() {
                BidMessageDialog bidMessageDialog = new BidMessageDialog();
                bidMessageDialog.setBid(mOutgoingBid);
                bidMessageDialog.setMessageListener(new BidMessageDialog.MessageListener() {
                    @Override
                    public void onMessageAdded(String message) {
                        mOutgoingBid.setMessage(message.length() > 0 ? message : null);
                    }

                    @Override
                    public void onBack() {
                        placeBid(false, true);
                    }
                });
                bidMessageDialog.show(getSupportFragmentManager(), bidMessageDialog
                        .getClass().getSimpleName());
            }
        });
        bidPriceDialog.show(getSupportFragmentManager(), bidPriceDialog.getClass()
                .getSimpleName());
    }

    private void sendBid() {
        final String key = mDatabase.child("bid-incoming/" + mProduct.getUserId()).push().getKey();
        Map<String, Object> bidMap = mOutgoingBid.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-outgoing/" + User.getLocalUser().getId() + "/" + key, bidMap);
        childUpdates.put("bid-incoming/" + mProduct.getUserId() + "/" + key, bidMap);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                DialogHelper.showDialogWithCloseAndDone(OneProductActivity.this, R.string.warning, task
                        .getException() != null && task.getException().getMessage() != null ?
                        task.getException().getMessage() : getString(R.string.error_unknown), null);
            }

            Counters.getInstance().increaseOutgoingBidsCount();

            // todo payment gateway
            changeStatusAfterSuccessfulPayment(key);
        });
    }

    private void changeStatusAfterSuccessfulPayment(final String key) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-outgoing/" + User.getLocalUser().getId() + "/" + key + "/status", 1);
        childUpdates.put("bid-incoming/" + mProduct.getUserId() + "/" + key + "/status", 1);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                DialogHelper.showDialogWithCloseAndDone(OneProductActivity.this, R.string.warning, task
                        .getException() != null && task.getException().getMessage() != null ?
                        task.getException().getMessage() : getString(R.string.error_unknown), null);
            }

            if (SwiprApp.getInstance().getSp().getBoolean(Constants.Prefs.SHOW_BID_SUCCESSFUL_ALERT, true)) {
                BidSuccessfulDialog bidSuccessfulDialog = new BidSuccessfulDialog();
                bidSuccessfulDialog.show(getSupportFragmentManager(),
                        bidSuccessfulDialog.getClass().getSimpleName());
            }
        });
    }
}
