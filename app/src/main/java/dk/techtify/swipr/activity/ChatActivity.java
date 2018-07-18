package dk.techtify.swipr.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.chat.ChatAdapter;
import dk.techtify.swipr.helper.FirebaseHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.helper.OneSignalHelper;
import dk.techtify.swipr.model.chat.ChatRoom;
import dk.techtify.swipr.model.chat.Message;
import dk.techtify.swipr.model.chat.MessageContent;
import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.push.OutgoingPush;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 12/10/2016.
 */
public class ChatActivity extends AppCompatActivity implements ChatAdapter.LastItemListener {

    public static final String EXTRA_RECIPIENT = "dk.techtify.swipr.activity.ChatActivity.EXTRA_RECIPIENT";
    public static final String EXTRA_CONTENT = "dk.techtify.swipr.activity.ChatActivity.EXTRA_CONTENT";

    public static final int PART_SIZE = 20;

    private Follow mRecipient;

    private ActionView mActionView;
    private RecyclerView mRecycler;
    private View mProgressBar;
    private ChatAdapter mAdapter;
    private EditText mEditable;

    private User mMe;

    private boolean mTheyInChat = false;
    private DatabaseReference mDatabase;
    private DatabaseReference mMeInChatRef, mTheyInChatRef;

    private MessageContent mMessageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecipient = (Follow) getIntent().getSerializableExtra(EXTRA_RECIPIENT);

        if (getIntent().hasExtra(EXTRA_CONTENT)) {
            mMessageContent = (MessageContent) getIntent().getSerializableExtra(EXTRA_CONTENT);
            getIntent().removeExtra(EXTRA_CONTENT);
        }

        mMe = User.getLocalUser();

        setContentView(R.layout.activity_chat);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mActionView = (ActionView) findViewById(R.id.action_view);
        mActionView.setMenuButton(R.drawable.ic_arrow_back);
        mActionView.setTitle(mRecipient.getName());
        mActionView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mActionView.getActionButton().setVisibility(View.INVISIBLE);
        mActionView.setMenuClickListener(new ActionView.MenuClickListener() {
            @Override
            public void onMenuClick() {
                onBackPressed();
            }
        });

        if (mRecipient.getPhotoUrl() != null && !mRecipient.getPhotoUrl().isEmpty()) {
            mActionView.getPhotoView().setVisibility(View.VISIBLE);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(mRecipient.getPhotoUrl()))
                    .into(mActionView.getPhotoView());
        }

        mProgressBar = findViewById(R.id.progress);

        mRecycler = (RecyclerView) findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        mAdapter = new ChatAdapter(this, this, mRecipient);
        mRecycler.setAdapter(mAdapter);

        mEditable = (EditText) findViewById(R.id.editable);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditable.getText().toString().trim().length() > 0 &&
                        NetworkHelper.isOnline(ChatActivity.this, NetworkHelper.ALERT)) {
                    sendMessage(mEditable.getText().toString().trim());
                }
            }
        });

        if (mAdapter.getItemCount() == 0 && NetworkHelper.isOnline(this, NetworkHelper.NONE)) {
            mDatabase.child("chat").child(mMe.getId()).child(mRecipient.getId()).limitToLast(PART_SIZE).addChildEventListener(mChildEventListener);
            mDatabase.child("chat-room").child(mMe.getId()).child(mRecipient.getId()).child("messageSeen").setValue(true);
        }

        connection();
    }

    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mAdapter.add(new Message(dataSnapshot.getKey().toString(), (Map<String, Object>) dataSnapshot.getValue()));
            mRecycler.scrollToPosition(0);
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
    };

    @Override
    public void onLastItemReached(Message message) {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.child("chat").child(User.getLocalUser().getId()).child(mRecipient.getId())
                .orderByChild("created").endAt(message.getCreated(), message.getId()).limitToLast(PART_SIZE + 1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                            return;
                        }

                        List<Message> newMessages = new ArrayList<>();
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        Iterator it = map.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            newMessages.add(new Message(pair.getKey(), (Map<String, Object>) pair.getValue()));
                        }
                        if (newMessages.size() > 0) {
                            mAdapter.addAll(newMessages);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void sendMessage(final String text) {
        mEditable.setText("");

        Message message;
        if (mMessageContent == null) {
            message = new Message(text, mMe.getId());
        } else {
            message = new Message(text, mMe.getId(), mMessageContent);
            mMessageContent = null;
        }
        final String messageId = mDatabase.child("chat").child(mMe.getId()).child(mRecipient.getId()).push().getKey();
        message.setId(messageId);

        ChatRoom myRoom = new ChatRoom(mRecipient.getName(), mRecipient.getPhotoUrl(), message, true);
        ChatRoom theirRoom = new ChatRoom(mMe.getName(), mMe.getPhotoUrl(), message, mTheyInChat);

        mAdapter.add(message);
        mRecycler.scrollToPosition(0);

        Map<String, Object> messageMap = message.toMap();
        Map<String, Object> myRoomMap = myRoom.toMap();
        Map<String, Object> theirRoomMap = theirRoom.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("chat/" + mMe.getId() + "/" + mRecipient.getId() + "/" + messageId, messageMap);
        childUpdates.put("chat/" + mRecipient.getId() + "/" + mMe.getId() + "/" + messageId, messageMap);
        childUpdates.put("chat-room/" + mMe.getId() + "/" + mRecipient.getId(), myRoomMap);
        childUpdates.put("chat-room/" + mRecipient.getId() + "/" + mMe.getId(), theirRoomMap);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    mAdapter.removeItem(messageId);
                }

                if (!mTheyInChat) {
                    OneSignalHelper.sendPush(new OutgoingPush(MessageContent.TYPE_PRODUCT_MESSAGE,
                            text, mMe.getPhotoUrl(), mMe.getName(), mRecipient.getId()));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onDestroy() {
        mDatabase.child("chat").child(mMe.getId()).child(mRecipient.getId()).removeEventListener(mChildEventListener);
        mMeInChatRef.removeValue();
        mTheyInChatRef.removeEventListener(mTheyInChatListener);

        super.onDestroy();
    }

    private void connection() {
        mMeInChatRef = FirebaseDatabase.getInstance().getReference(
                "user-data/" + mMe.getId() + "/connection/chat-room/" + mRecipient.getId());
        mMeInChatRef.setValue(true);

        mTheyInChatRef = FirebaseDatabase.getInstance().getReference(
                "user-data/" + mRecipient.getId() + "/connection/chat-room/" + mMe.getId());
        mTheyInChatRef.addValueEventListener(mTheyInChatListener);
    }

    private ValueEventListener mTheyInChatListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            mTheyInChat = snapshot != null && snapshot.getValue() != null && snapshot.getValue(Boolean.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
