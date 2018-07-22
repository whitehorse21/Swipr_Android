package dk.techtify.swipr.helper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import dk.techtify.swipr.asynctask.ApiResponseListener;
import dk.techtify.swipr.asynctask.DeleteProductAsyncTask;
import dk.techtify.swipr.model.chat.ChatRoom;
import dk.techtify.swipr.model.chat.Message;
import dk.techtify.swipr.model.chat.MessageContent;
import dk.techtify.swipr.model.chat.MessageContentDataProduct;
import dk.techtify.swipr.model.profile.IncomingBid;
import dk.techtify.swipr.model.push.OutgoingPush;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/26/2017.
 */

public class FirebaseHelper {

    public static void increaseCounter(String userId, String counterName) {
        increaseCounter(userId, counterName, null);
    }

    public static void increaseCounter(String userId, String counterName, final OnSuccessListener listener) {
        FirebaseDatabase.getInstance().getReference().child("counter").child(userId).child(counterName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer value = mutableData.getValue(Integer.class);
                if (value == null) {
                    value = 0;
                }

                value += 1;

                mutableData.setValue(value);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    return;
                }
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    public static void decreaseCounter(String userId, String counterName) {
        decreaseCounter(userId, counterName, null);
    }

    public static void decreaseCounter(String userId, String counterName, final OnSuccessListener listener) {
        FirebaseDatabase.getInstance().getReference().child("counter").child(userId).child(counterName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer value = mutableData.getValue(Integer.class);
                if (value == null) {
                    return Transaction.success(mutableData);
                }

                value -= 1;

                mutableData.setValue(value);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    return;
                }
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    public static void expireIncomingBid(final IncomingBid bid) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-outgoing/" + bid.getBidderId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_EXPIRED);
        childUpdates.put("bid-incoming/" + User.getLocalUser().getId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_EXPIRED);
        database.updateChildren(childUpdates);
    }

    public static void expireOutgoingBid(final IncomingBid bid) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("bid-incoming/" + bid.getSellerId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_EXPIRED);
        childUpdates.put("bid-outgoing/" + User.getLocalUser().getId() + "/" + bid.getId() + "/status", IncomingBid.STATUS_EXPIRED);
        database.updateChildren(childUpdates);
    }

    public static void sendActionBidMessage(String text, IncomingBid ib, SellerBuyer b, int type) {
        User me = User.getLocalUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Message message;
        if (type == 0) {
            message = new Message(text, me.getId());
        } else {
            message = new Message(text, me.getId(), new MessageContent(type, new MessageContentDataProduct(ib.getProductId(), ib.getSellerId())));
        }
        final String messageId = database.child("chat").child(me.getId()).child(ib.getBidderId()).push().getKey();
        message.setId(messageId);

        ChatRoom myRoom = new ChatRoom(b.getName(), b.getPhotoUrl(), message, true);
        ChatRoom theirRoom = new ChatRoom(me.getName(), me.getPhotoUrl(), message, false);

        Map<String, Object> messageMap = message.toMap();
        Map<String, Object> myRoomMap = myRoom.toMap();
        Map<String, Object> theirRoomMap = theirRoom.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("chat/" + me.getId() + "/" + b.getId() + "/" + messageId, messageMap);
        childUpdates.put("chat/" + b.getId() + "/" + me.getId() + "/" + messageId, messageMap);
        childUpdates.put("chat-room/" + me.getId() + "/" + b.getId(), myRoomMap);
        childUpdates.put("chat-room/" + b.getId() + "/" + me.getId(), theirRoomMap);

        database.updateChildren(childUpdates);

        OneSignalHelper.sendPush(new OutgoingPush(type, b));
    }

    public static void sendNewBidMessage(String text, String productId, SellerBuyer seller) {
        User me = User.getLocalUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Message message = new Message(text, me.getId(), new MessageContent(MessageContent.TYPE_NEW_BID,
                new MessageContentDataProduct(productId, seller.getId())));

        final String messageId = database.child("chat").child(me.getId()).child(seller.getId()).push().getKey();
        message.setId(messageId);

        ChatRoom myRoom = new ChatRoom(seller.getName(), seller.getPhotoUrl(), message, true);
        ChatRoom theirRoom = new ChatRoom(me.getName(), me.getPhotoUrl(), message, false);

        Map<String, Object> messageMap = message.toMap();
        Map<String, Object> myRoomMap = myRoom.toMap();
        Map<String, Object> theirRoomMap = theirRoom.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("chat/" + me.getId() + "/" + seller.getId() + "/" + messageId, messageMap);
        childUpdates.put("chat/" + seller.getId() + "/" + me.getId() + "/" + messageId, messageMap);
        childUpdates.put("chat-room/" + me.getId() + "/" + seller.getId(), myRoomMap);
        childUpdates.put("chat-room/" + seller.getId() + "/" + me.getId(), theirRoomMap);

        database.updateChildren(childUpdates);

        OneSignalHelper.sendPush(new OutgoingPush(MessageContent.TYPE_NEW_BID, seller));
    }

    public static void getBidder(String bidderId, final OnResultListener onResultListener) {
        FirebaseDatabase.getInstance().getReference().child("user-data").child(bidderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onDataChange(DataSnapshot ds) {
                        if (ds != null && ds.getValue() != null) {
                            onResultListener.onResult(new SellerBuyer(ds.getKey(),
                                    (Map<String, Object>) ds.getValue()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void deleteMyProduct(final String userId, final String productId, final ApiResponseListener listener) {
        new DeleteProductAsyncTask(userId, productId, new ApiResponseListener() {
            @Override
            public void onSuccess(Object object) {

            }

            @Override
            public void onError(Object object) {
                if (listener != null) {
                    listener.onError(null);
                }
            }
        }).execute();
    }

    public interface OnSuccessListener {
        void onSuccess();
    }

    public interface OnResultListener {
        void onResult(Object o);
    }

    public static void increaseRating(String productId, boolean amIseller, final String userId, final int rating, final OnSuccessListener listener) {
        FirebaseDatabase.getInstance().getReference().child("user-product").child(amIseller ? User
                .getLocalUser().getId() : userId).child(productId).child(amIseller ? "ratingLeftBySeller" : "ratingLeftByBuyer")
                .setValue(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }

            FirebaseDatabase.getInstance().getReference().child("user-data").child(userId).child("ratingTotal")
                    .runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer value = mutableData.getValue(Integer.class);
                            if (value == null) {
                                value = 0;
                            }

                            value += rating;

                            mutableData.setValue(value);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                return;
                            }
                            FirebaseDatabase.getInstance().getReference().child("user-data").child(userId).child("ratingVotesNumber")
                                    .runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            Integer value = mutableData.getValue(Integer.class);
                                            if (value == null) {
                                                value = 0;
                                            }

                                            value += 1;

                                            mutableData.setValue(value);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            if (databaseError != null) {
                                                return;
                                            }
                                            if (listener != null) {
                                                listener.onSuccess();
                                            }
                                        }
                                    });
                        }
                    });
        });
    }
}
