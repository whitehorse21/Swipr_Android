package dk.techtify.swipr.dialog.store;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.RatingBar;

/**
 * Created by Pavel on 1/20/2017.
 */

public class SellerBuyerDialog extends BaseDialog {

    private static final String SELLER_BUYER = "dk.techtify.swipr.dialog.sell.SellerBuyerDialog.SELLER_BUYER";

    private DatabaseReference mDatabase;

    private SellerBuyer mSellerBuyer;
    private CheckBox mFollow;
    private TextView mSold, mPurchased, mFollowers;

    private long mTheirFollowersCount = -1;

    private Boolean mFollowingToTheir;

    public static SellerBuyerDialog newInstance(SellerBuyer sellerBuyer) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SELLER_BUYER, sellerBuyer);
        SellerBuyerDialog fragment = new SellerBuyerDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSellerBuyer = (SellerBuyer) getArguments().getSerializable(SELLER_BUYER);
        View view = inflater.inflate(R.layout.dialog_seller_buyer, null);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        view.findViewById(R.id.close).setOnClickListener(view1 -> getDialog().dismiss());

        (view.findViewById(R.id.plus_member)).setVisibility(mSellerBuyer.isPlusMember() ? View.VISIBLE : View.INVISIBLE);
        ((TextView) view.findViewById(R.id.seller_name)).setText(mSellerBuyer.getName());

        if (!TextUtils.isEmpty(mSellerBuyer.getPhotoUrl())) {
            GlideApp.with(getActivity())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(mSellerBuyer.getPhotoUrl()))
                    .into((ImageView) view.findViewById(R.id.seller_photo));
        }
        if (mSellerBuyer.getContactInfo() != null) {
            ((TextView) view.findViewById(R.id.address)).setText(mSellerBuyer.getContactInfo().getCity());
        }
        ((TextView) view.findViewById(R.id.created)).setText(DateTimeHelper.getFormattedDate(mSellerBuyer.getCreated(), "dd.MM.yyyy"));

        mSold = view.findViewById(R.id.sold);
        mPurchased = view.findViewById(R.id.purchased);
        mFollowers = view.findViewById(R.id.followers);

        mFollow = view.findViewById(R.id.follow);
        mFollow.setOnClickListener(v -> {
            if (!NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                mFollow.setChecked(!mFollow.isChecked());
                return;
            }
            mFollow.setEnabled(false);
            if (mFollow.isChecked()) {
                followSeller();
            } else {
                unfollowSeller();
            }
        });
        mFollow.setOnCheckedChangeListener((buttonView, isChecked) -> mFollow.setText(getString(isChecked ? R.string.following : R.string.follow)));

        view.findViewById(R.id.overflow).setOnClickListener(v -> showOverflowMenu());

        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.NONE)) {
            mDatabase.child("counter").child(mSellerBuyer.getId())
                    .addValueEventListener(mCountersListener);
            mDatabase.child("follow").child(User.getLocalUser().getId()).child("following")
                    .child(mSellerBuyer.getId())
                    .addValueEventListener(mFollowingListener);
        }

        return view;
    }

    private void showOverflowMenu() {
        BottomSheet.Builder sheet = new BottomSheet.Builder(getActivity());
        sheet.sheet(R.id.sheet_report_abuse, null, getResources().getString(R.string.report_abuse));
        sheet.sheet(R.menu.menu_cancel).listener((dialog, which) -> {
            switch (which) {
                case R.id.sheet_report_abuse:
                    break;
                case R.id.cancel:
                    break;
            }
        }).show();
    }

    private void followSeller() {
        User user = User.getLocalUser();

        Follow they = new Follow(mSellerBuyer.getId(), mSellerBuyer.getName(), mSellerBuyer.getPhotoUrl());
        Follow me = new Follow(user.getId(), user.getName(), user.getPhotoUrl());

        Map<String, Object> myMap = they.toMap();
        Map<String, Object> theirMap = me.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("follow/" + user.getId() + "/following/" + mSellerBuyer.getId(), myMap);
        childUpdates.put("follow/" + mSellerBuyer.getId() + "/followers/" + user.getId(), theirMap);
        childUpdates.put("counter/" + user.getId() + "/following/", Counters.getInstance().getFollowing() + 1);
        childUpdates.put("counter/" + mSellerBuyer.getId() + "/followers/", mTheirFollowersCount + 1);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                mFollow.setChecked(!mFollow.isChecked());
            }

            mFollow.setEnabled(true);
        });
    }

    private void unfollowSeller() {
        User user = User.getLocalUser();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("follow/" + user.getId() + "/following/" + mSellerBuyer.getId(), null);
        childUpdates.put("follow/" + mSellerBuyer.getId() + "/followers/" + user.getId(), null);
        childUpdates.put("counter/" + user.getId() + "/following/", Counters.getInstance().getFollowing() - 1);
        childUpdates.put("counter/" + mSellerBuyer.getId() + "/followers/", mTheirFollowersCount - 1);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                mFollow.setChecked(!mFollow.isChecked());
            }

            mFollow.setEnabled(true);
        });
    }

    ValueEventListener mCountersListener = new ValueEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (SellerBuyerDialog.this.isAdded()) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    mTheirFollowersCount = 0;
                } else {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    mTheirFollowersCount = Integer.parseInt(map.containsKey("followers") ?
                            (map.get("followers").toString()) : "0");

                    mSold.setText(map.containsKey("sold") ? (map.get("sold").toString()) : "0");
                    mPurchased.setText(map.containsKey("purchased") ? (map.get("purchased").toString()) : "0");
                    mFollowers.setText(String.valueOf(mTheirFollowersCount));
                }

                if (mFollowingToTheir != null && mTheirFollowersCount > -1) {
                    mFollow.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener mFollowingListener = new ValueEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (SellerBuyerDialog.this.isAdded()) {
                mFollowingToTheir = dataSnapshot != null && dataSnapshot.getValue() != null;

                mFollow.setChecked(mFollowingToTheir);

                if (mFollowingToTheir != null && mTheirFollowersCount > -1) {
                    mFollow.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDestroyView() {
        mDatabase.child("counter").child(mSellerBuyer.getId()).removeEventListener(mCountersListener);
        mDatabase.child("follow").child(User.getLocalUser().getId()).child("following")
                .child(mSellerBuyer.getId()).removeEventListener(mFollowingListener);
        super.onDestroyView();
    }
}