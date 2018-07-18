package dk.techtify.swipr.fragment.store;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.store.StoreItemAdapter;
import dk.techtify.swipr.dialog.store.SellerBuyerDialog;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.RatingBar;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by Pavel on 15/11/2016.
 */

public class StoreItemHolderFragment extends Fragment implements ValueEventListener {

    private static final int PART_SIZE = 5;

    private Product mProduct;
    private boolean mDownloadOtherItems;

    public void setDownloadOtherItems(boolean downloadOtherItems) {
        this.mDownloadOtherItems = downloadOtherItems;
    }

    public Product getProduct() {
        return mProduct;
    }

    public void setProduct(Product mProduct) {
        this.mProduct = mProduct;
    }

    private SellerBuyer mSeller;

    private View mPlusLabel;
    private TextView mSellerName;
//    private RatingBar mSellerRating;
    private ImageView mSellerPhoto;
    private StoreItemAdapter mAdapter;

    private DatabaseReference mDatabase;

    public static StoreItemHolderFragment newInstance(Product product, boolean downloadOtherItems) {
        StoreItemHolderFragment fragment = new StoreItemHolderFragment();
        fragment.setProduct(product);
        fragment.setDownloadOtherItems(downloadOtherItems);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.fragment_store_item_holder, null);

        mPlusLabel = view.findViewById(R.id.plus_member);
        mSellerName = (TextView) view.findViewById(R.id.seller_name);
//        mSellerRating = (RatingBar) view.findViewById(R.id.rating);
        mSellerPhoto = (ImageView) view.findViewById(R.id.seller_photo);

        VerticalViewPager itemPager = (VerticalViewPager) view.findViewById(R.id.pager);
        mAdapter = new StoreItemAdapter(getChildFragmentManager(), mProduct, mSeller);
        itemPager.setAdapter(mAdapter);
        itemPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                StoreItemFragment.increaseViewCounter(mAdapter.getList().get(position), false);

                if (position > 0 && position == mAdapter.getCount() - 1) {
                    getOtherProductsOfUser(mAdapter.getList().get(mAdapter.getCount() - 1));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mSeller != null) {
            setSellerBuyerFields();
            mAdapter.setSeller(mSeller);
        } else if (NetworkHelper.isOnline(getActivity(), NetworkHelper.NONE)) {
            getSellerInfo();
        }

        return view;
    }

    private void getSellerInfo() {
        mDatabase.child("user-data").child(mProduct.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot ds) {
                if (ds == null || ds.getValue() == null) {
                    return;
                }
                mSeller = new SellerBuyer(ds.getKey(), (Map<String, Object>) ds.getValue());
                setSellerBuyerFields();
                mAdapter.setSeller(mSeller);

                if (mDownloadOtherItems) {
                    getOtherProductsOfUser(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getOtherProductsOfUser(Product lastProduct) {
        if (AppConfig.DEBUG) {
            Log.d("ITEM HOLDER FRAGMENT", "End of vertical list is reached. Requesting other items");
        }
        if (lastProduct != null) {
            Log.d("ITEM HOLDER FRAGMENT", "Last id is " + lastProduct.getId());
            mDatabase.child("user-product").child(mProduct.getUserId()).orderByChild("created")
                    .endAt(lastProduct.getCreated(), lastProduct.getId())
                    .limitToLast(PART_SIZE).addListenerForSingleValueEvent(this);
        } else {
            mDatabase.child("user-product").child(mProduct.getUserId()).orderByChild("created")
                    .limitToLast(PART_SIZE).addListenerForSingleValueEvent(this);
        }
    }

    private void setSellerBuyerFields() {
        mPlusLabel.setVisibility(mSeller.isPlusMember() ? View.VISIBLE : View.INVISIBLE);
        mSellerName.setText(mSeller.getName());
//        mSellerRating.setRating(mSeller.getRating());
        if (mSeller.getPhotoUrl() != null && !mSeller.getPhotoUrl().isEmpty()) {
            try {
                Glide.with(getActivity())
                        .using(new FirebaseImageLoader())
                        .load(FirebaseStorage.getInstance().getReferenceFromUrl(mSeller.getPhotoUrl()))
                        .into(mSellerPhoto);
            } catch (Exception e) {
                if (AppConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        mSellerName.setOnClickListener(mUserClickListener);
//        mSellerRating.setOnClickListener(mUserClickListener);
        mSellerPhoto.setOnClickListener(mUserClickListener);
    }

    private View.OnClickListener mUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (User.checkSignIn(getActivity())) {
                SellerBuyerDialog uid = SellerBuyerDialog.newInstance(mSeller);
                uid.show(getActivity().getSupportFragmentManager(), uid.getClass().getSimpleName());
            }
        }
    };

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (StoreItemHolderFragment.this.isAdded() && dataSnapshot != null && dataSnapshot.getValue() != null) {
            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
            Iterator it = map.entrySet().iterator();
            List<Product> newProducts = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Product p = new Product(pair.getKey().toString(), (Map<String, Object>) pair.getValue());
                if (p.getStatus()== 0 && !mAdapter.getList().contains(p)) {
                    newProducts.add(p);
                }
            }
            if (newProducts.size() > 0) {
                mAdapter.addAll(newProducts);
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
