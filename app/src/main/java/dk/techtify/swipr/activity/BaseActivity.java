package dk.techtify.swipr.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.bid.IncomingBidHolderDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.FirebaseHelper;
import dk.techtify.swipr.helper.StringHelper;
import dk.techtify.swipr.model.ServerTime;
import dk.techtify.swipr.model.chat.MessageContent;
import dk.techtify.swipr.model.profile.IncomingBid;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/24/2017.
 */

public class BaseActivity extends AppCompatActivity {

    public static final String TAG_BIDS_DIALOG = "IncomingBidHolderDialog";

    private static List<IncomingBid> sIncomingBids;

    private static List<String> sOutgoingBids, sBoughtItems;

    public static List<IncomingBid> getIncomingBids() {
        return sIncomingBids;
    }

    public static List<String> getOutgoingBids() {
        return sOutgoingBids;
    }

    public static List<String> getBoughtItems() {
        return sBoughtItems;
    }

    public static void setIncomingBids(List<IncomingBid> incomingBids) {
        sIncomingBids = incomingBids;
    }

    public static void setOutgoingBids(List<String> sOutgoingBids) {
        BaseActivity.sOutgoingBids = sOutgoingBids;
    }

    DatabaseReference mCounterReference;

    DatabaseReference mIncomingBidsReference;

    DatabaseReference mOutgoingBidsReference;

    private long mLastVibrationTime;

    private IncomeBidsListener mIncomeBidsListener;

    public void setIncomeBidsListener(IncomeBidsListener incomeBidsListener) {
        this.mIncomeBidsListener = incomeBidsListener;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (User.getLocalUser().getId() != null) {
            if (sIncomingBids == null) {
                sIncomingBids = new ArrayList<>();
            }

            if (sOutgoingBids == null) {
                sOutgoingBids = new ArrayList<>();
            }

            if (sBoughtItems == null) {
                sBoughtItems = new ArrayList<>();
            }

            mCounterReference = FirebaseDatabase.getInstance().getReference().child("counter").child(
                    User.getLocalUser().getId());
            mIncomingBidsReference = FirebaseDatabase.getInstance().getReference().child("bid-incoming")
                    .child(User.getLocalUser().getId());
            mOutgoingBidsReference = FirebaseDatabase.getInstance().getReference().child("bid-outgoing")
                    .child(User.getLocalUser().getId());
            FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            double offset = snapshot.getValue(Double.class);
                            ServerTime.setOffset(offset);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });

            mOutgoingBidsReference.orderByChild("status").equalTo(IncomingBid.STATUS_ACCEPTED).limitToFirst(5)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                                Counters.getInstance().updateOutgoingBidsCount(0);
                                return;
                            }

                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            IncomingBid ib = new IncomingBid(dataSnapshot.getKey().toString(), map);
                            if (sBoughtItems == null) {
                                sBoughtItems = new ArrayList<>();
                            }
                            sBoughtItems.add(ib.getProductId());
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            mOutgoingBidsReference.orderByChild("status").equalTo(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        Counters.getInstance().updateOutgoingBidsCount(0);
                        return;
                    }

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    IncomingBid ib = new IncomingBid(dataSnapshot.getKey().toString(), map);
                    if (sOutgoingBids == null) {
                        sOutgoingBids = new ArrayList<>();
                    }
                    if (ib.hasExpired()) {
                        FirebaseHelper.expireOutgoingBid(ib);
                    } else if (!sOutgoingBids.contains(ib.getProductId())) {
                        sOutgoingBids.add(ib.getProductId());
                        Counters.getInstance().increaseOutgoingBidsCount();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        Counters.getInstance().updateOutgoingBidsCount(0);
                        return;
                    }

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    IncomingBid ib = new IncomingBid(dataSnapshot.getKey().toString(), map);
                    if (sOutgoingBids != null && sOutgoingBids.contains(ib.getProductId())) {
                        sOutgoingBids.remove(ib.getProductId());
                        Counters.getInstance().decreaseOutgoingBidsCount();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCounterReference != null) {
            mCounterReference.addValueEventListener(mCountersListener);
        }
        if (mIncomingBidsReference != null) {
            mIncomingBidsReference.orderByChild("status").equalTo(1).addChildEventListener(mIncomingBidsListener);
        }
    }

    @Override
    public void onPause() {
        if (mCounterReference != null) {
            mCounterReference.removeEventListener(mCountersListener);
        }
        if (mIncomingBidsReference != null) {
            mIncomingBidsReference.removeEventListener(mIncomingBidsListener);
        }
        super.onPause();
    }

    private ValueEventListener mCountersListener = new ValueEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Map<String, Object> map = dataSnapshot != null && dataSnapshot.getValue()
                    != null ? (Map<String, Object>) dataSnapshot.getValue() :
                    new HashMap<String, Object>();

            Counters.getInstance().update(map);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mIncomingBidsListener = new ChildEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (AppConfig.DEBUG) {
                Log.d("INCOMING BID", "added bid " + dataSnapshot.getKey());
            }
            IncomingBid incomingBid = new IncomingBid(dataSnapshot.getKey(), (Map<String, Object>)
                    dataSnapshot.getValue());

            if (incomingBid.hasExpired()) {
                FirebaseHelper.expireIncomingBid(incomingBid);
                return;
            }

            if (!sIncomingBids.contains(incomingBid)) {
                sIncomingBids.add(incomingBid);
                if (mIncomeBidsListener != null) {
                    mIncomeBidsListener.bidAdded(incomingBid);
                }

                showBidHolderDialog(true);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @SuppressWarnings("unchecked")
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (AppConfig.DEBUG) {
                Log.d("INCOMING BID", "removed bid " + dataSnapshot.getKey());
            }
            IncomingBid incomingBid = new IncomingBid(dataSnapshot.getKey(), (Map<String, Object>)
                    dataSnapshot.getValue());

            if (sIncomingBids.contains(incomingBid)) {
                sIncomingBids.remove(incomingBid);
                if (mIncomeBidsListener != null) {
                    mIncomeBidsListener.bidRemoved(incomingBid);
                }

                showBidHolderDialog(false);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void showBidHolderDialog(boolean forAddingAction) {
        IncomingBidHolderDialog incomingBidHolderDialog = (IncomingBidHolderDialog)
                getSupportFragmentManager().findFragmentByTag(TAG_BIDS_DIALOG);

        if (incomingBidHolderDialog != null) {
            incomingBidHolderDialog.dismiss();

            if (!forAddingAction && sIncomingBids.size() == 0) {
                return;
            }
        } else if (!forAddingAction) {
            return;
        }

        incomingBidHolderDialog = new IncomingBidHolderDialog();
        incomingBidHolderDialog.addIncomingBids(sIncomingBids);
        incomingBidHolderDialog.show(getSupportFragmentManager(), TAG_BIDS_DIALOG);
        getSupportFragmentManager().executePendingTransactions();

        if (forAddingAction) {
            long currentTime = System.currentTimeMillis();
            if (mLastVibrationTime + 2000 < currentTime) {
                mLastVibrationTime = currentTime;
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(new long[]{0, 70, 70, 70, 70, 70}, -1);
            }
        }
    }


    public void declineIncomingBid(final IncomingBid bid) {
        declineIncomingBid(bid, null);
    }

    public void declineIncomingBid(final IncomingBid bid, final SellerBuyer bidder) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-outgoing/" + bid.getBidderId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_DECLINED);
        childUpdates.put("bid-incoming/" + User.getLocalUser().getId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_DECLINED);

        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    DialogHelper.showDialogWithCloseAndDone(BaseActivity.this, R.string.warning, task
                            .getException() != null && task.getException().getMessage() != null ?
                            task.getException().getMessage() : getString(R.string.error_unknown), null);
                }

                if (bidder == null) {
                    FirebaseHelper.getBidder(bid.getBidderId(), new FirebaseHelper.OnResultListener() {
                        @Override
                        public void onResult(Object o) {
                            FirebaseHelper.sendActionBidMessage(getString(R.string.the_bid_declined).toUpperCase(),
                                    bid, (SellerBuyer) o, MessageContent.TYPE_BID_DECLINED);
                            String mess = bid.getDeclineMessage();
                            if (mess != null && mess.trim().length() > 0) {
                                FirebaseHelper.sendActionBidMessage(mess.trim(), bid, (SellerBuyer) o, 0);
                            }
                        }
                    });
                } else {
                    FirebaseHelper.sendActionBidMessage(getString(R.string.the_bid_declined).toUpperCase(),
                            bid, bidder, MessageContent.TYPE_BID_DECLINED);
                    String mess = bid.getDeclineMessage();
                    if (mess != null && mess.trim().length() > 0) {
                        FirebaseHelper.sendActionBidMessage(mess.trim(), bid, bidder, 0);
                    }
                }
            }
        });
    }

    public void acceptIncomingBid(final IncomingBid bid, final SellerBuyer bidder) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("bid-outgoing/" + bid.getBidderId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_ACCEPTED);
        childUpdates.put("bid-incoming/" + User.getLocalUser().getId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_ACCEPTED);

        final List<IncomingBid> theseBids = new ArrayList<>();

        for (IncomingBid ib : sIncomingBids) {
            if (ib.getProductId().equals(bid.getProductId())) {
                theseBids.add(ib);
                if (!ib.getBidderId().equals(bid.getBidderId())) {
                    childUpdates.put("bid-outgoing/" + ib.getBidderId() + "/" + ib.getId() + "/status", IncomingBid.STATUS_DECLINED);
                    childUpdates.put("bid-incoming/" + User.getLocalUser().getId() + "/" + ib.getId() + "/status", IncomingBid.STATUS_DECLINED);
                }
            }
        }
        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    DialogHelper.showDialogWithCloseAndDone(BaseActivity.this, R.string.warning, task
                            .getException() != null && task.getException().getMessage() != null ?
                            task.getException().getMessage() : getString(R.string.error_unknown), null);
                }

                FirebaseHelper.deleteMyProduct(bid.getSellerId(), bid.getProductId(), null);

                FirebaseHelper.increaseCounter(User.getLocalUser().getId(), "sold", new FirebaseHelper.OnSuccessListener() {
                    @Override
                    public void onSuccess() {
                        FirebaseHelper.increaseCounter(bid.getBidderId(), "purchased");
                    }
                });

                for (IncomingBid ib : theseBids) {
                    if (ib.getId().equals(bid.getId())) {
                        FirebaseHelper.sendActionBidMessage(getString(R.string.the_bid_accepted).toUpperCase(),
                                bid, bidder, MessageContent.TYPE_BID_ACCEPTED);
                        FirebaseHelper.sendActionBidMessage(StringHelper.getMyAddress(), bid, bidder, 0);
                    } else {
                        FirebaseHelper.getBidder(ib.getBidderId(), new FirebaseHelper.OnResultListener() {
                            @Override
                            public void onResult(Object o) {
                                FirebaseHelper.sendActionBidMessage(getString(R.string.the_bid_declined).toUpperCase(),
                                        bid, (SellerBuyer) o, MessageContent.TYPE_BID_DECLINED);
                            }
                        });
                    }
                }
            }
        });
    }

    public void cancelOutgoingBid(final IncomingBid bid) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-incoming/" + bid.getSellerId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_CANCELED);
        childUpdates.put("bid-outgoing/" + User.getLocalUser().getId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_CANCELED);

        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    DialogHelper.showDialogWithCloseAndDone(BaseActivity.this, R.string.warning, task
                            .getException() != null && task.getException().getMessage() != null ?
                            task.getException().getMessage() : getString(R.string.error_unknown), null);
                }
                if (sOutgoingBids.contains(bid.getProductId())) {
                    sOutgoingBids.remove(bid.getProductId());
                }
            }
        });

        FirebaseHelper.getBidder(bid.getSellerId(), new FirebaseHelper.OnResultListener() {
            @Override
            public void onResult(Object o) {
                FirebaseHelper.sendActionBidMessage(getString(R.string.bid_cancelled).toUpperCase(),
                        bid, (SellerBuyer) o, MessageContent.TYPE_BID_CANCELED);
            }
        });
    }

    public interface IncomeBidsListener {
        void bidAdded(IncomingBid bid);

        void bidRemoved(IncomingBid bid);
    }
}
