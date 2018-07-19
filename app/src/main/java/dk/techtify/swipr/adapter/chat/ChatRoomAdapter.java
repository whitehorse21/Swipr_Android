package dk.techtify.swipr.adapter.chat;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.wunderlist.slidinglayer.SlidingLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.ChatActivity;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.chat.ChatRoom;
import dk.techtify.swipr.model.push.OutgoingPush;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/4/2017.
 */

public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ChatRoom> mList;
    private LastItemListener mLastItemListener;

    private String mPreviousLastItem;
    private String myId;

    private final FirebaseStorage mStorage;

    private ParentSwipeListener mParentSwipeListener;
    private ActionListener mActionListener;

    public ChatRoomAdapter(Context context, LastItemListener lastItemListener, ParentSwipeListener
            parentSwipeListener, ActionListener actionListener) {
        this.mContext = context;
        this.mList = new ArrayList<>();
        this.mLastItemListener = lastItemListener;

        mParentSwipeListener = parentSwipeListener;
        mActionListener = actionListener;

        mStorage = FirebaseStorage.getInstance();

        mPreviousLastItem = "";
        myId = User.getLocalUser().getId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, null);
        RecyclerView.ViewHolder holder = new MessageHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ChatRoom chatRoom = mList.get(position);

        MessageHolder h = (MessageHolder) holder;

        h.root.setTag(chatRoom);
        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        h.root.setPadding(0, DisplayHelper.dpToPx(mContext, position == 0 ? 12 : 0), 0, 0);

        h.alphable.setBackgroundResource(chatRoom.isMessageSeen() ?
                R.drawable.selector_item_dark : R.drawable.selector_item_primary);
        h.name.setTextColor(ContextCompat.getColor(mContext, chatRoom.isMessageSeen() ?
                R.color.colorPrimary : android.R.color.white));
        h.time.setTextColor(ContextCompat.getColor(mContext, chatRoom.isMessageSeen() ?
                R.color.textSecondary : android.R.color.white));
        h.text.setTextColor(ContextCompat.getColor(mContext, chatRoom.isMessageSeen() ?
                R.color.textPrimary : android.R.color.white));

        setText(chatRoom.getMessageText(), h.text);

        h.name.setText(chatRoom.getUserName());
        h.time.setText(DateTimeHelper.showMessageTime(mContext, false, chatRoom.getMessageCreated()));

        if (!TextUtils.isEmpty(chatRoom.getUserPhotoUrl())) {
            GlideApp.with(mContext)
                    .load(mStorage.getReferenceFromUrl(chatRoom.getUserPhotoUrl()))
                    .into(h.photo);
        } else {
            h.photo.setImageDrawable(null);
        }

        if (position == mList.size() - 1 && mList.size() % ChatActivity.PART_SIZE == 0 &&
                !mPreviousLastItem.equals(chatRoom.getUserId()) &&
                NetworkHelper.isOnline(mContext, NetworkHelper.NONE)) {
            mPreviousLastItem = chatRoom.getUserId();
            mLastItemListener.onLastItemReached(chatRoom);
        }
    }

    private void setText(String text, TextView textView) {
        if (text.equalsIgnoreCase(OutgoingPush.BID_ACCEPTED_DK) || text.equalsIgnoreCase(OutgoingPush.BID_ACCEPTED_EN)) {
            textView.setText(mContext.getString(R.string.the_bid_accepted).toUpperCase());
        } else if (text.equalsIgnoreCase(OutgoingPush.BID_CANCELED_DK) || text.equalsIgnoreCase(OutgoingPush.BID_CANCELED_EN)) {
            textView.setText(mContext.getString(R.string.bid_cancelled).toUpperCase());
        } else if (text.equalsIgnoreCase(OutgoingPush.BID_DECLINED_DK) || text.equalsIgnoreCase(OutgoingPush.BID_DECLINED_EN)) {
            textView.setText(mContext.getString(R.string.the_bid_declined).toUpperCase());
        } else if (text.equalsIgnoreCase(OutgoingPush.NEW_BID_DK) || text.equalsIgnoreCase(OutgoingPush.NEW_BID_EN)) {
            textView.setText(mContext.getString(R.string.new_bid).toUpperCase());
        } else {
            textView.setText(text);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<ChatRoom> list) {
        for (int i = list.size() - 1; i > -1; i--) {
            if (mList.contains(list.get(i))) {
                list.remove(i);
                break;
            }
        }
        Collections.sort(list, new CreatedComparator());
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(ChatRoom chatRoom) {
        for (int i = 0; i < mList.size(); i++) {
            ChatRoom cr = mList.get(i);
            if (cr.getUserId().equals(chatRoom.getUserId())) {
                boolean swap = cr.getMessageCreated() != chatRoom.getMessageCreated();

                cr.setMessageCreated(chatRoom.getMessageCreated());
                cr.setMessageSeen(chatRoom.isMessageSeen());
                cr.setMessageSenderId(chatRoom.getMessageSenderId());
                cr.setMessageText(chatRoom.getMessageText());

                notifyItemChanged(i);
                if (swap) {
                    Collections.swap(mList, i, 0);
                    notifyItemMoved(i, 0);
                }
                return;
            }
        }
        mList.add(0, chatRoom);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void removeItem(String userId) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getUserId().equals(userId)) {
                mList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, getItemCount());
                break;
            }
        }
    }

    private class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final int width, offset;

        private FrameLayout root;
        private SlidingLayer swipeable;
        private View alphable, delete;
        private ImageView photo;
        private TextView name, time, text;

        public MessageHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView.findViewById(R.id.root);
            swipeable = (SlidingLayer) itemView.findViewById(R.id.swipeable);
            alphable = itemView.findViewById(R.id.alphable);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            delete = itemView.findViewById(R.id.delete);
            name = (TextView) itemView.findViewById(R.id.name);
            time = (TextView) itemView.findViewById(R.id.time);
            text = (TextView) itemView.findViewById(R.id.text);

            width = DisplayHelper.getScreenResolution(mContext)[0];
            offset = width - DisplayHelper.dpToPx(mContext, 96);

            swipeable.openLayer(false);
            swipeable.setOffsetDistance(offset);
            swipeable.setOnScrollListener(new SlidingLayer.OnScrollListener() {
                @Override
                public void onScroll(final int absoluteScroll) {
                    mParentSwipeListener.onParentSwipeEnable(!(absoluteScroll < width));

                    float relativeScroll = (float) absoluteScroll / (float) width;
                    alphable.setAlpha(relativeScroll);
                }
            });

            alphable.setOnClickListener(this);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.alphable) {
                mActionListener.onChatRoomClick((ChatRoom) root.getTag());
            } else {
                swipeable.openLayer(true);
                mActionListener.onChatRoomDelete((ChatRoom) root.getTag());
            }
        }
    }

    private class CreatedComparator implements Comparator<ChatRoom> {

        @Override
        public int compare(ChatRoom o1, ChatRoom o2) {
            return ((Long) o2.getMessageCreated()).compareTo(o1.getMessageCreated());
        }
    }

    public interface LastItemListener {
        void onLastItemReached(ChatRoom chatRoom);
    }

    public interface ParentSwipeListener {
        void onParentSwipeEnable(boolean enable);
    }

    public interface ActionListener {
        void onChatRoomDelete(ChatRoom chatRoom);

        void onChatRoomClick(ChatRoom chatRoom);
    }
}