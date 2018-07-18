package dk.techtify.swipr.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.activity.ChatActivity;
import dk.techtify.swipr.activity.profile.FollowersActivity;
import dk.techtify.swipr.adapter.chat.ChatRoomAdapter;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.chat.ChatRoom;
import dk.techtify.swipr.model.chat.Recipient;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.ActionView;
import dk.techtify.swipr.view.SwipeLinearLayoutManager;

/**
 * Created by Pavel on 15/11/2016.
 */

public class MessagesFragment extends Fragment implements ActionView.ActionClickListener, ChatRoomAdapter.LastItemListener, ChatRoomAdapter.ActionListener {

    private static final int PART_SIZE = 10;

    private RecyclerView mRecycler;
    private ChatRoomAdapter mAdapter;
    private View mProgressBar;
    private DatabaseReference mDatabase;
    private User mMe;

    private InternetStatusReceiver mInternetStatusReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setContainerTopMargin(true);
        ((MainActivity) getActivity()).getActionView().setTitle(R.string.messages);
        ((MainActivity) getActivity()).getActionView().setActionButton(R.drawable.ic_new_message, this);
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), R.color.colorPrimary));

        mMe = User.getLocalUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.fragment_messages, null);

        mProgressBar = view.findViewById(R.id.progress);

        mRecycler = (RecyclerView) view.findViewById(R.id.recycler);
        final SwipeLinearLayoutManager layoutManager = new SwipeLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(layoutManager);
        mAdapter = new ChatRoomAdapter(getActivity(), this, new ChatRoomAdapter.ParentSwipeListener() {
            @Override
            public void onParentSwipeEnable(boolean enable) {
                layoutManager.setScrollEnabled(enable);
            }
        }, this);
        mRecycler.setAdapter(mAdapter);

        if (mAdapter.getItemCount() == 0) {
            if (NetworkHelper.isOnline(getActivity(), view)) {
                mDatabase.child("chat-room").child(mMe.getId()).orderByChild("messageCreated")
                        .limitToLast(PART_SIZE).addChildEventListener(mChildEventListener);
            } else {
                mInternetStatusReceiver = new InternetStatusReceiver();
                getActivity().registerReceiver(mInternetStatusReceiver, new IntentFilter(
                        "android.net.conn.CONNECTIVITY_CHANGE"));
            }
        }

        return view;
    }

    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mAdapter.add(new ChatRoom(dataSnapshot.getKey().toString(), (Map<String, Object>)
                    dataSnapshot.getValue()));
            mRecycler.scrollToPosition(0);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            mAdapter.add(new ChatRoom(dataSnapshot.getKey().toString(), (Map<String, Object>)
                    dataSnapshot.getValue()));
            mRecycler.scrollToPosition(0);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mAdapter.removeItem(dataSnapshot.getKey().toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDestroyView() {
        if (mInternetStatusReceiver != null) {
            try {
                getActivity().unregisterReceiver(mInternetStatusReceiver);
            } catch (Exception e) {
                if (AppConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        super.onDestroyView();
    }

    @Override
    public void onActionButtonClick() {
        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
            Intent intent = new Intent(getActivity(), FollowersActivity.class);
            intent.putExtra(FollowersActivity.EXTRA_MODE, FollowersActivity.MODE_MESSAGE);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onLastItemReached(ChatRoom chatRoom) {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.child("chat-room").child(mMe.getId())
                .orderByChild("messageCreated").endAt(chatRoom.getMessageCreated(), chatRoom.getUserId())
                .limitToLast(PART_SIZE + 1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (MessagesFragment.this.isAdded()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        return;
                    }

                    List<ChatRoom> newRooms = new ArrayList<>();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Iterator it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        newRooms.add(new ChatRoom(pair.getKey().toString(),
                                (Map<String, Object>) pair.getValue()));
                    }
                    if (newRooms.size() > 0) {
                        mAdapter.addAll(newRooms);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onChatRoomClick(ChatRoom chatRoom) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_RECIPIENT, new Recipient(chatRoom));
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onChatRoomDelete(final ChatRoom chatRoom) {
        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.delete_chat_room, new DialogHelper.OnActionListener() {
                        @Override
                        public void onPositive(Object o) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("chat/" + mMe.getId() + "/" + chatRoom.getUserId(), null);
                            childUpdates.put("chat-room/" + mMe.getId() + "/" + chatRoom.getUserId(), null);
                            mDatabase.updateChildren(childUpdates);
                        }
                    });
        }
    }

    @Override
    public void onDetach() {
        mDatabase.child("chat-room").child(mMe.getId()).removeEventListener(mChildEventListener);

        super.onDetach();
    }

    private class InternetStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (AppConfig.DEBUG) {
                        Log.d("NETWORK", "WIFI " + activeNetwork.isConnected());
                    }
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (AppConfig.DEBUG) {
                        Log.d("NETWORK", "MOBILE " + activeNetwork.isConnected());
                    }
                }
                mDatabase.child("chat-room").child(mMe.getId()).orderByChild("messageCreated")
                        .limitToLast(PART_SIZE).addChildEventListener(mChildEventListener);
                getActivity().unregisterReceiver(this);
                mInternetStatusReceiver = null;
            }
        }
    }
}
