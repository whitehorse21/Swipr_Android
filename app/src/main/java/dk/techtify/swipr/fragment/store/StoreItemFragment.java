package dk.techtify.swipr.fragment.store;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.SwiprApp;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.activity.ChatActivity;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.activity.store.ProductDetailsActivity;
import dk.techtify.swipr.asynctask.AddToFavoritesAsyncTask;
import dk.techtify.swipr.asynctask.ApiResponseListener;
import dk.techtify.swipr.asynctask.DeleteFromFavoritesAsyncTask;
import dk.techtify.swipr.asynctask.ProductSeenAsyncTask;
import dk.techtify.swipr.dialog.main.SwiprPlusDialog;
import dk.techtify.swipr.dialog.store.BidMessageDialog;
import dk.techtify.swipr.dialog.store.BidOverviewDialog;
import dk.techtify.swipr.dialog.store.BidPriceDialog;
import dk.techtify.swipr.dialog.store.BidSuccessfulDialog;
import dk.techtify.swipr.dialog.store.BidTimerDialog;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.FirebaseHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.chat.MessageContent;
import dk.techtify.swipr.model.chat.MessageContentDataProduct;
import dk.techtify.swipr.model.chat.Recipient;
import dk.techtify.swipr.model.store.OutgoingBid;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 15/11/2016.
 */

public class StoreItemFragment extends Fragment {

    private int mPosition = -1;

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    private SellerBuyer mSeller;

    public void setSeller(SellerBuyer seller) {
        this.mSeller = seller;
    }

    private Product mProduct;

    public void setProduct(Product mProduct) {
        this.mProduct = mProduct;
    }

    private OutgoingBid mOutgoingBid;

    private DatabaseReference mDatabase;

    public static StoreItemFragment newInstance(int position, Product product, SellerBuyer seller) {
        StoreItemFragment fragment = new StoreItemFragment();
        fragment.setPosition(position);
        fragment.setProduct(product);
        fragment.setSeller(seller);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.fragment_store_item, null);

        ((TextView) view.findViewById(R.id.name)).setText(mProduct.getName());
        if (mProduct.getSize() == null || mProduct.getSize().isEmpty()) {
            view.findViewById(R.id.size).setVisibility(View.INVISIBLE);
        } else {
            view.findViewById(R.id.size).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.size)).setText(mProduct.getSize());
        }
        ((TextView) view.findViewById(R.id.price)).setText(TextUtils.concat(String.valueOf(mProduct
                .getPrice()), " ", getResources().getString(R.string.kr)));
        ((TextView) view.findViewById(R.id.location)).setText(mProduct.getContactInfo().getCity());

        final CheckBox like = (CheckBox) view.findViewById(R.id.like);
        like.setChecked(mProduct.isAddedToFavorites());
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!User.checkSignIn(getActivity())) {
                    like.setChecked(false);
                    return;
                }
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    if (like.isChecked()) {
                        new AddToFavoritesAsyncTask(mProduct.getId(), new ApiResponseListener() {
                            @Override
                            public void onSuccess(Object object) {
                            }

                            @Override
                            public void onError(Object object) {
                                if (StoreItemFragment.this.isAdded()) {
                                    Integer code = (Integer) object;
                                    if (code == 400) {
                                        return;
                                    } else if (code == 223) {
                                        SwiprPlusDialog spd = new SwiprPlusDialog();
                                        spd.setDealListener((MainActivity) getActivity());
                                        spd.show(getActivity().getSupportFragmentManager(), spd.getClass().getSimpleName());
                                        return;
                                    }

                                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                            R.string.error_unknown, null);
                                }
                            }
                        }).execute();
                    } else {
                        new DeleteFromFavoritesAsyncTask(mProduct.getId(), new ApiResponseListener() {
                            @Override
                            public void onSuccess(Object object) {
                            }

                            @Override
                            public void onError(Object object) {
                                if (StoreItemFragment.this.isAdded()) {
                                    like.setChecked(!like.isChecked());
                                }
                            }
                        }).execute();
                    }
                }
            }
        });

        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User.checkSignIn(getActivity())) {
                    if (mSeller != null) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra(ChatActivity.EXTRA_CONTENT, new MessageContent(MessageContent
                                .TYPE_PRODUCT_MESSAGE, new MessageContentDataProduct(mProduct.getId(),
                                mProduct.getUserId(), mProduct.getPhotos().get(0))));
                        intent.putExtra(ChatActivity.EXTRA_RECIPIENT, new Recipient(mSeller));
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            }
        });

        ImageView photoView = (ImageView) view.findViewById(R.id.photo);
        if (mProduct.getPhotos() != null && mProduct.getPhotos().size() > 0 && !TextUtils.isEmpty(mProduct.getPhotos().get(0))) {
            GlideApp.with(getActivity())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(mProduct.getPhotos().get(0)))
                    .into(photoView);
        }

        photoView.setOnClickListener(openImagesListener);
        view.findViewById(R.id.text_layout).setOnClickListener(openImagesListener);

        view.findViewById(R.id.bid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BaseActivity.getOutgoingBids().contains(mProduct.getId())) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.you_have_active_bid, null);
                    return;
                }
                if (BaseActivity.getBoughtItems().contains(mProduct.getId())) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.you_have_accepted_bid, null);
                    return;
                }
//                if (!SwiprApp.getInstance().getSp().contains(Constants.Prefs.IS_BID_FEE_ALERT_SHOWN)) {
//                    BidFeeAlertDialog bidFeeAlertDialog = new BidFeeAlertDialog();
//                    bidFeeAlertDialog.setNextListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            placeBid(true, false);
//                        }
//                    });
//                    bidFeeAlertDialog.show(getActivity().getSupportFragmentManager(),
//                            bidFeeAlertDialog.getClass().getSimpleName());
//
//                    SwiprApp.getInstance().getSp().edit().putBoolean(Constants.Prefs
//                            .IS_BID_FEE_ALERT_SHOWN, true).apply();
//                } else if (mProduct != null && mSeller != null) {
                placeBid(true, false);
//                }
            }
        });

        return view;
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
                        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                            sendBid();
                        }
                    }

                    @Override
                    public void onBack() {
                        placeBid(false, true);
                    }
                });
                bidOverviewDialog.show(getActivity().getSupportFragmentManager(), bidOverviewDialog
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
                bidTimerDialog.show(getActivity().getSupportFragmentManager(), bidTimerDialog
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
                bidMessageDialog.show(getActivity().getSupportFragmentManager(), bidMessageDialog
                        .getClass().getSimpleName());
            }
        });
        bidPriceDialog.show(getActivity().getSupportFragmentManager(), bidPriceDialog.getClass()
                .getSimpleName());
    }

    private void sendBid() {
        final String key = mDatabase.child("bid-incoming/" + mProduct.getUserId()).push().getKey();
        Map<String, Object> bidMap = mOutgoingBid.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-outgoing/" + User.getLocalUser().getId() + "/" + key, bidMap);
        childUpdates.put("bid-incoming/" + mProduct.getUserId() + "/" + key, bidMap);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (StoreItemFragment.this.isAdded()) {
                    if (!task.isSuccessful()) {
                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning, task
                                .getException() != null && task.getException().getMessage() != null ?
                                task.getException().getMessage() : getString(R.string.error_unknown), null);
                    }

                    Counters.getInstance().increaseOutgoingBidsCount();

                    // todo payment gateway
                    changeStatusAfterSuccessfulPayment(key);
                }
            }
        });
    }

    private void changeStatusAfterSuccessfulPayment(final String key) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-outgoing/" + User.getLocalUser().getId() + "/" + key + "/status", 1);
        childUpdates.put("bid-incoming/" + mProduct.getUserId() + "/" + key + "/status", 1);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning, task
                            .getException() != null && task.getException().getMessage() != null ?
                            task.getException().getMessage() : getString(R.string.error_unknown), null);
                }

                FirebaseHelper.sendNewBidMessage(getString(R.string.new_bid).toUpperCase(), mProduct.getId(), mSeller);

                if (SwiprApp.getInstance().getSp().getBoolean(Constants.Prefs.SHOW_BID_SUCCESSFUL_ALERT, true)) {
                    BidSuccessfulDialog bidSuccessfulDialog = new BidSuccessfulDialog();
                    bidSuccessfulDialog.show(getActivity().getSupportFragmentManager(),
                            bidSuccessfulDialog.getClass().getSimpleName());
                }
            }
        });
    }

    private View.OnClickListener openImagesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mProduct.getPhotos() != null && mProduct.getPhotos().size() > 0) {
                Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                intent.putExtra(ProductDetailsActivity.EXTRA_TITLE, mProduct.getName());
                intent.putExtra(ProductDetailsActivity.EXTRA_DESCRIPTION, mProduct.getDescription());
                intent.putExtra(ProductDetailsActivity.EXTRA_PHOTOS, mProduct.getPhotos());
                getActivity().startActivity(intent);
            }
        }
    };

    public static void increaseViewCounter(final Product product, boolean toSecondServer) {
        final String key = DateTimeHelper.getFormattedDate(System.currentTimeMillis(), "yyyyMMdd");
        final Set<String> set = SwiprApp.getInstance().getSp().getStringSet("views" + key, new ArraySet<String>());
        if (!set.contains(product.getId())) {
            if (toSecondServer) {
                new ProductSeenAsyncTask(User.getLocalUser().getId(), product.getId())
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            FirebaseDatabase.getInstance().getReference("user-product/" + product.getUserId() + "/"
                    + product.getId() + "/views").runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Integer views = mutableData.getValue(Integer.class);
                    if (views == null) {
                        return Transaction.success(mutableData);
                    }

                    views += 1;

                    mutableData.setValue(views);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                    try {
                        set.add(product.getId());
                        SwiprApp.getInstance().getSp().edit().putStringSet("views" + key, set).apply();
                    } catch (Exception e) {
                        if (AppConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}