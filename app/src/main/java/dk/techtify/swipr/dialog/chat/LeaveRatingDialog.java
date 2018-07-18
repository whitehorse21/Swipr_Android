package dk.techtify.swipr.dialog.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.FirebaseHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/20/2017.
 */

public class LeaveRatingDialog extends BaseDialog {

    private String mProductId, mSellerId;
    private Follow mOtherPerson;

    private RatingBar mLeaveRating;

    private TextView mTitle;

    public void setProductAndSellerId(String productId, String sellerId) {
        mProductId = productId;
        mSellerId = sellerId;
    }

    public void setOtherPerson(Follow otherPerson) {
        mOtherPerson = otherPerson;
    }

    private DatabaseReference mDatabase;

    private SellerBuyer mSellerBuyer;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_leave_rating, null);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        mTitle = (TextView) view.findViewById(R.id.title);
        mLeaveRating = (RatingBar) view.findViewById(R.id.leave_rating);

        ((TextView) view.findViewById(R.id.seller_name)).setText(mOtherPerson.getName());
        if (mOtherPerson.getPhotoUrl() != null && !mOtherPerson.getPhotoUrl().isEmpty()) {
            Glide.with(getActivity())
                    .using(new FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(mOtherPerson.getPhotoUrl()))
                    .into((ImageView) view.findViewById(R.id.seller_photo));
        }

        mDatabase.child("user-product").child(mSellerId).child(mProductId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null || !LeaveRatingDialog.this.isAdded()) {
                    return;
                }
                Product product = new Product(dataSnapshot.getKey().toString(), (Map<String, Object>) dataSnapshot.getValue());
                mTitle.setVisibility(View.VISIBLE);
                if (mSellerId.equals(User.getLocalUser().getId()) && product.isRatingLeftBySeller()) {
                    mTitle.setText(getString(R.string.rating_already_provided));
                } else if (!(mSellerId.equals(User.getLocalUser().getId())) && product.isRatingLeftByBuyer()) {
                    mTitle.setText(getString(R.string.rating_already_provided));
                } else {
                    mTitle.setText(getString(R.string.leave_rating));
                    mLeaveRating.setVisibility(View.VISIBLE);
                }

                getOtherPersonData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeaveRating.getRating() == 0) {
                    getDialog().dismiss();
                } else if (mLeaveRating.getRating() > 0 && NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    saveRating();
                }
            }
        });

        return view;
    }

    private void saveRating() {
        FirebaseHelper.increaseRating(mProductId, mSellerId.equals(User.getLocalUser().getId()), mOtherPerson.getId(), (int) mLeaveRating.getRating(), null);
        getDialog().dismiss();
    }

    private void getOtherPersonData() {
        mDatabase.child("user-data").child(mOtherPerson.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null || !LeaveRatingDialog.this.isAdded()) {
                    return;
                }

                mSellerBuyer = new SellerBuyer(dataSnapshot.getKey().toString(),
                        (Map<String, Object>) dataSnapshot.getValue());

                (view.findViewById(R.id.plus_member)).setVisibility(mSellerBuyer.isPlusMember() ? View.VISIBLE : View.INVISIBLE);
                ((dk.techtify.swipr.view.RatingBar) view.findViewById(R.id.rating)).setRating(mSellerBuyer.getRating());
                if (mSellerBuyer.getContactInfo() != null) {
                    ((TextView) view.findViewById(R.id.address)).setText(mSellerBuyer.getContactInfo().getCity());
                }
                ((TextView) view.findViewById(R.id.created)).setText(TextUtils.concat(getString(R.string.member_since), "\n",
                        DateTimeHelper.getFormattedDate(mSellerBuyer.getCreated(), "dd.MM.yyyy")));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}