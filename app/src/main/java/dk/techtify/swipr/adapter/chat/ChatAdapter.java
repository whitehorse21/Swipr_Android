package dk.techtify.swipr.adapter.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.ChatActivity;
import dk.techtify.swipr.activity.chat.OneProductActivity;
import dk.techtify.swipr.dialog.chat.LeaveRatingDialog;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.chat.Message;
import dk.techtify.swipr.model.chat.MessageContent;
import dk.techtify.swipr.model.chat.MessageContentDataProduct;
import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/4/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Message> mList;
    private LastItemListener mLastItemListener;
    private int mWidth;
    private Follow mOtherPerson;

    private String mPreviousLastItem;
    private String myId;

    public ChatAdapter(Context context, LastItemListener lastItemListener, Follow otherPerson) {
        this.mContext = context;
        this.mList = new ArrayList<>();
        this.mLastItemListener = lastItemListener;
        this.mOtherPerson = otherPerson;

        mWidth = DisplayHelper.getScreenResolution(context)[0];

        mPreviousLastItem = "";
        myId = User.getLocalUser().getId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, null);
        RecyclerView.ViewHolder holder = new MessageHolder(layoutView);
        return holder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Message message = mList.get(position);

        MessageHolder h = (MessageHolder) holder;

        h.root.setTag(message);

        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        h.root.setPadding(0, DisplayHelper.dpToPx(mContext, 12),
                0, DisplayHelper.dpToPx(mContext, position == 0 ? 46 : 0));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) h.bidImage.getLayoutParams();
        h.bidImage.setVisibility(View.GONE);

        h.textMy.setTypeface(Typeface.DEFAULT);
        h.textMy.setMaxWidth(mWidth);
        h.textTheir.setTypeface(Typeface.DEFAULT);
        h.textTheir.setMaxWidth(mWidth);

        if (message.getSenderId().equals(myId)) {
            params.gravity = Gravity.RIGHT;

            h.layoutMy.setVisibility(View.VISIBLE);
            h.textMy.setText(message.getText());
            h.timeMy.setText(DateTimeHelper.showMessageTime(mContext, false, message.getCreated()));

            h.layoutTheir.setVisibility(View.GONE);

            if (message.getMessageContent() != null) {
                if (message.getMessageContent().getType() == MessageContent.TYPE_PRODUCT_MESSAGE) {
                    h.contentMy.setVisibility(View.VISIBLE);
                    h.photoMy.setVisibility(View.VISIBLE);

                    MessageContentDataProduct mcdp = (MessageContentDataProduct) message.getMessageContent().getData();
                    String url = mcdp.getPhotoUrl();
                    if (url != null) {
                        Glide.with(mContext)
                                .using(new FirebaseImageLoader())
                                .load(FirebaseStorage.getInstance().getReferenceFromUrl(url))
                                .into(h.photoMy);
                    }

                    h.textMy.setMaxWidth(DisplayHelper.dpToPx(mContext, 180));
                } else {
                    h.textMy.setTypeface(h.textMy.getTypeface(), Typeface.ITALIC);
                    h.bidImage.setVisibility(View.VISIBLE);
                    h.contentMy.setVisibility(View.GONE);
                    h.photoMy.setVisibility(View.GONE);
                    if (message.getMessageContent().getType() == MessageContent.TYPE_NEW_BID) {
                        h.textMy.setText(mContext.getString(R.string.new_bid).toUpperCase());
                    } else if (message.getMessageContent().getType() == MessageContent.TYPE_BID_DECLINED) {
                        h.textMy.setText(mContext.getString(R.string.the_bid_declined).toUpperCase());
                    } else if (message.getMessageContent().getType() == MessageContent.TYPE_BID_ACCEPTED) {
                        h.textMy.setText(mContext.getString(R.string.the_bid_accepted).toUpperCase());
                    } else if (message.getMessageContent().getType() == MessageContent.TYPE_BID_CANCELED) {
                        h.textMy.setText(mContext.getString(R.string.bid_cancelled).toUpperCase());
                    }
                }
            } else {
                h.contentMy.setVisibility(View.GONE);
                h.photoMy.setVisibility(View.GONE);
            }
        } else {
            params.gravity = Gravity.LEFT;

            h.layoutTheir.setVisibility(View.VISIBLE);
            h.textTheir.setText(message.getText());
            h.timeTheir.setText(DateTimeHelper.showMessageTime(mContext, false, message.getCreated()));

            h.layoutMy.setVisibility(View.GONE);

            if (message.getMessageContent() != null) {
                if (message.getMessageContent().getType() == MessageContent.TYPE_PRODUCT_MESSAGE) {
                    h.contentTheir.setVisibility(View.VISIBLE);
                    h.photoTheir.setVisibility(View.VISIBLE);

                    MessageContentDataProduct mcdp = (MessageContentDataProduct) message.getMessageContent().getData();
                    String url = mcdp.getPhotoUrl();
                    if (url != null) {
                        Glide.with(mContext)
                                .using(new FirebaseImageLoader())
                                .load(FirebaseStorage.getInstance().getReferenceFromUrl(url))
                                .into(h.photoTheir);
                    }

                    h.textTheir.setMaxWidth(DisplayHelper.dpToPx(mContext, 180));
                } else {
                    h.textTheir.setTypeface(h.textMy.getTypeface(), Typeface.ITALIC);
                    h.bidImage.setVisibility(View.VISIBLE);
                    h.contentTheir.setVisibility(View.GONE);
                    h.photoTheir.setVisibility(View.GONE);
                    if (message.getMessageContent().getType() == MessageContent.TYPE_NEW_BID) {
                        h.textTheir.setText(mContext.getString(R.string.new_bid).toUpperCase());
                    } else if (message.getMessageContent().getType() == MessageContent.TYPE_BID_DECLINED) {
                        h.textTheir.setText(mContext.getString(R.string.the_bid_declined).toUpperCase());
                    } else if (message.getMessageContent().getType() == MessageContent.TYPE_BID_ACCEPTED) {
                        h.textTheir.setText(mContext.getString(R.string.the_bid_accepted).toUpperCase());
                    } else if (message.getMessageContent().getType() == MessageContent.TYPE_BID_CANCELED) {
                        h.textTheir.setText(mContext.getString(R.string.bid_cancelled).toUpperCase());
                    }
                }
            } else {
                h.contentTheir.setVisibility(View.GONE);
                h.photoTheir.setVisibility(View.GONE);
            }
        }

        if (position == mList.size() - 1 && mList.size() % ChatActivity.PART_SIZE == 0 &&
                !mPreviousLastItem.equals(message.getId()) &&
                NetworkHelper.isOnline(mContext, NetworkHelper.NONE)) {
            mPreviousLastItem = message.getId();
            mLastItemListener.onLastItemReached(message);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<Message> list) {
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

    public void add(Message message) {
        for (Message m : mList) {
            if (m.getId().equals(message.getId())) {
                m.setId(message.getId());
                m.setCreated(message.getCreated());
                return;
            }
        }
        mList.add(0, message);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void removeItem(String messageId) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getId().equals(messageId)) {
                mList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, getItemCount());
                break;
            }
        }
    }

    private class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout root;
        private FrameLayout contentMy, contentTheir;
        private ImageView photoMy, photoTheir, bidImage;
        private LinearLayout layoutMy, layoutTheir;
        private TextView timeMy, textMy, textTheir, timeTheir;

        public MessageHolder(View itemView) {
            super(itemView);
            root = (LinearLayout) itemView.findViewById(R.id.root);

            bidImage = (ImageView) itemView.findViewById(R.id.bid);

            contentMy = (FrameLayout) itemView.findViewById(R.id.content_layout_my);
            layoutMy = (LinearLayout) itemView.findViewById(R.id.layout_my);
            photoMy = (ImageView) itemView.findViewById(R.id.photo_my);
            timeMy = (TextView) itemView.findViewById(R.id.time_my);
            textMy = (TextView) itemView.findViewById(R.id.text_my);

            contentTheir = (FrameLayout) itemView.findViewById(R.id.content_layout_they);
            layoutTheir = (LinearLayout) itemView.findViewById(R.id.layout_they);
            photoTheir = (ImageView) itemView.findViewById(R.id.photo_they);
            textTheir = (TextView) itemView.findViewById(R.id.text_they);
            timeTheir = (TextView) itemView.findViewById(R.id.time_they);

            contentMy.setOnClickListener(this);
            contentTheir.setOnClickListener(this);
            bidImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Message message = (Message) root.getTag();
            if (message.getMessageContent() != null && message.getMessageContent().getData()
                    instanceof MessageContentDataProduct && NetworkHelper.isOnline(mContext, NetworkHelper.ALERT)) {
                MessageContentDataProduct mcdp = (MessageContentDataProduct) message.getMessageContent().getData();
                if (message.getMessageContent().getType() == MessageContent.TYPE_BID_ACCEPTED) {
                    LeaveRatingDialog lrd = new LeaveRatingDialog();
                    lrd.setProductAndSellerId(mcdp.getId(), mcdp.getSellerId());
                    lrd.setOtherPerson(mOtherPerson);
                    lrd.show(((AppCompatActivity) mContext).getSupportFragmentManager(), lrd.getClass().getSimpleName());
                    return;
                }
                Intent intent = new Intent(mContext, OneProductActivity.class);
                intent.putExtra(OneProductActivity.EXTRA_ID, mcdp.getId());
                intent.putExtra(OneProductActivity.EXTRA_SELLER_ID, mcdp.getSellerId());
                if (mcdp.getPhotoUrl() != null) {
                    intent.putExtra(OneProductActivity.EXTRA_PHOTO_URL, mcdp.getPhotoUrl());
                }
                mContext.startActivity(intent);
            }
        }
    }

    private class CreatedComparator implements Comparator<Message> {

        @Override
        public int compare(Message o1, Message o2) {
            return ((Long) o2.getCreated()).compareTo(o1.getCreated());
        }
    }

    public interface LastItemListener {
        void onLastItemReached(Message message);
    }
}