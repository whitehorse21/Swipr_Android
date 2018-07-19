package dk.techtify.swipr.dialog.bid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.wunderlist.slidinglayer.SlidingLayer;

import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.profile.IncomingBid;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.view.HammerView;
import dk.techtify.swipr.view.RatingBar;

/**
 * Created by Pavel on 1/20/2017.
 */

public class IncomingBidDialog extends Fragment {

    private DatabaseReference mDatabase;

    private IncomingBid mIncomingBid;

    private SellerBuyer mBidder;

    private HammerView mHammer;

    private int mPosition, mCounter;

    private View mPlusMemberView;
    private TextView mBidderNameView, mBidderCityView, mBidderCreatedView;
    private RatingBar mBidderRatingView;
    private ImageView mBidderImageView;

    public void setIncomingBid(IncomingBid incomingBid) {
        this.mIncomingBid = incomingBid;
    }

    public void setPositionAndCounter(int position, int counter) {
        this.mPosition = position;
        this.mCounter = counter;
    }

    public static IncomingBidDialog newInstance(IncomingBid incomingBid, int position, int counter) {
        IncomingBidDialog fragment = new IncomingBidDialog();
        fragment.setIncomingBid(incomingBid);
        fragment.setPositionAndCounter(position, counter);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_incoming_bid, null);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mPosition != mCounter - 1) {
            view.findViewById(R.id.next).setVisibility(View.VISIBLE);
            view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollNext();
                }
            });
        }

        if (mPosition != 0) {
            view.findViewById(R.id.previous).setVisibility(View.VISIBLE);
            view.findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollPrevious();
                }
            });
        }

        mPlusMemberView = view.findViewById(R.id.plus_member);
        mBidderNameView = (TextView) view.findViewById(R.id.seller_name);
        mBidderRatingView = (RatingBar) view.findViewById(R.id.rating);
        mBidderImageView = (ImageView) view.findViewById(R.id.seller_photo);
        mBidderCityView = (TextView) view.findViewById(R.id.address);
        mBidderCreatedView = (TextView) view.findViewById(R.id.created);

        view.findViewById(R.id.inc_shipping).setVisibility(mIncomingBid.isIncludeShipping() ? View.VISIBLE : View.INVISIBLE);
        if (mIncomingBid.getMessage() != null) {
            TextView message = (TextView) view.findViewById(R.id.message);
            message.setVisibility(View.VISIBLE);
            message.setText(mIncomingBid.getMessage());
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), getString(R.string.message),
                            mIncomingBid.getMessage(), true, null);
                }
            });
        }
        ((TextView) view.findViewById(R.id.product_name)).setText(TextUtils.concat(mIncomingBid
                .getBrand().getName(), " ", mIncomingBid.getType().getName()));
        ((TextView) view.findViewById(R.id.price)).setText(TextUtils.concat(String.valueOf(
                mIncomingBid.getBid()), " ", getString(R.string.kr)));

        final TextView attachedMessage = (TextView) view.findViewById(R.id.attached_message);
        if (mIncomingBid.getDeclineMessage() != null) {
            attachedMessage.setText(mIncomingBid.getDeclineMessage());
        }
        attachedMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mIncomingBid.setDeclineMessage(attachedMessage.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final View attachArrow = view.findViewById(R.id.attach_message_arrow);
        final SlidingLayer sl = (SlidingLayer) view.findViewById(R.id.sliding);
        if (mIncomingBid.isDeclineMessageOpen()) {
            sl.openLayer(false);
        }
        sl.setOnScrollListener(new SlidingLayer.OnScrollListener() {
            @Override
            public void onScroll(int absoluteScroll) {
                float relativeScroll = (float) absoluteScroll / (float) sl.getHeight();
                attachArrow.setRotation(270 + 180 * relativeScroll);
            }
        });
        sl.setOnInteractListener(new SlidingLayer.OnInteractListener() {
            @Override
            public void onOpen() {
                mIncomingBid.setDeclineMessageOpen(true);
            }

            @Override
            public void onShowPreview() {

            }

            @Override
            public void onClose() {
                mIncomingBid.setDeclineMessageOpen(false);
                IoHelper.hideKeyboard(getActivity(), attachedMessage);
            }

            @Override
            public void onOpened() {

            }

            @Override
            public void onPreviewShowed() {

            }

            @Override
            public void onClosed() {

            }
        });

        view.findViewById(R.id.attach_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sl.isOpened()) {
                    sl.closeLayer(true);
                } else if (sl.isClosed()) {
                    sl.openLayer(true);
                }
            }
        });

        view.findViewById(R.id.decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIncomingBid.hasExpired()) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.bid_has_expired_decline, null);
                    return;
                }
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    ((BaseActivity) getActivity()).declineIncomingBid(mIncomingBid, mBidder);
                }
            }
        });

        mHammer = (HammerView) view.findViewById(R.id.hammer);
        mHammer.setOnBidAcceptedListener(new HammerView.OnBidAcceptedListener() {
            @Override
            public void onBidAccepted() {
                if (mIncomingBid.hasExpired()) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.bid_has_expired_decline, null);
                    mHammer.setDefault();
                    return;
                }
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    ((BaseActivity) getActivity()).acceptIncomingBid(mIncomingBid, mBidder);
                } else {
                    mHammer.setDefault();
                }
            }
        });

        update();

        return view;
    }

    private void scrollNext() {
        IncomingBidHolderDialog incomingBidHolderDialog = (IncomingBidHolderDialog)
                getActivity().getSupportFragmentManager().findFragmentByTag(BaseActivity
                        .TAG_BIDS_DIALOG);
        incomingBidHolderDialog.getViewPager().setCurrentItem(mPosition + 1);
    }

    private void scrollPrevious() {
        IncomingBidHolderDialog incomingBidHolderDialog = (IncomingBidHolderDialog)
                getActivity().getSupportFragmentManager().findFragmentByTag(BaseActivity
                        .TAG_BIDS_DIALOG);
        incomingBidHolderDialog.getViewPager().setCurrentItem(mPosition - 1);
    }

    private void update() {
        mDatabase.child("user-data").child(mIncomingBid.getBidderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    mBidder = new SellerBuyer(dataSnapshot.getKey(), (Map<String, Object>) dataSnapshot.getValue());

                    refreshSellerFields();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void refreshSellerFields() {
        mPlusMemberView.setVisibility(mBidder.isPlusMember() ? View.VISIBLE : View.INVISIBLE);
        mBidderNameView.setText(mBidder.getName());
        mBidderRatingView.setRating(mBidder.getRating());
        if (mBidder.getContactInfo() != null) {
            mBidderCityView.setText(mBidder.getContactInfo().getCity());
        }
        mBidderCreatedView.setText(TextUtils.concat(getString(R.string
                .user_since), "\n", DateTimeHelper.getFormattedDate(mBidder.getCreated(), "dd.MM.yyyy")));
        if (!TextUtils.isEmpty(mBidder.getPhotoUrl())) {
            GlideApp.with(getActivity())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(mBidder.getPhotoUrl()))
                    .into(mBidderImageView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}