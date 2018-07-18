package dk.techtify.swipr.adapter.profile;

import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.wunderlist.slidinglayer.SlidingLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.model.ServerTime;
import dk.techtify.swipr.model.profile.IncomingBid;
import dk.techtify.swipr.view.CounterTextView;

/**
 * Created by Pavel on 1/4/2017.
 */

public class ActiveBidsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private int mMode;
    private List<IncomingBid> mList;

    private FirebaseStorage mStorage;

    private ParentSwipeListener mParentSwipeListener;
    private ActionListener mActionListener;

    public ActiveBidsAdapter(Context context, int mode, List<IncomingBid> list, ParentSwipeListener
            parentSwipeListener, ActionListener actionListener) {
        this.mContext = context;
        this.mMode = mode;
        this.mList = new ArrayList<>();
        this.mList.addAll(list);
        Collections.sort(mList, new CreatedComparator());

        mStorage = FirebaseStorage.getInstance();

        mParentSwipeListener = parentSwipeListener;
        mActionListener = actionListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_bids, null);
        RecyclerView.ViewHolder holder = new ActivePostHolder(layoutView);
        return holder;
    }

    @SuppressWarnings("NumericOverflow")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        IncomingBid bid = mList.get(position);

        ActivePostHolder h = (ActivePostHolder) holder;
        h.root.setTag(bid);
        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayHelper.dpToPx(mContext, 112)));

        if (bid.getProductPhotoUrl() != null) {
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(mStorage.getReferenceFromUrl(bid.getProductPhotoUrl()))
                    .into(h.photo);
        } else {
            h.photo.setImageDrawable(null);
        }

        h.brand.setText(bid.getBrand().getName());
        h.initialPrice.setText(TextUtils.concat(String.valueOf(bid.getInitialPrice()), " ", mContext.getString(R.string.kr)));
        h.bid.setText(TextUtils.concat(String.valueOf(bid.getBid()), " ", mContext.getString(R.string.kr)));

        h.time.setTime(bid.getCreated() + bid.getExpirationTime() - ServerTime.getServerTime());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addItem(IncomingBid bid) {
        if (mList.contains(bid)) {
            return;
        }
        mList.add(0, bid);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void addAll(List<IncomingBid> incomingBids) {
        for (IncomingBid ib : incomingBids) {
            if (!mList.contains(ib)) {
                mList.add(ib);
            }
        }
        Collections.sort(mList, new CreatedComparator());
        notifyDataSetChanged();
    }

    public void removeItem(IncomingBid bid) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).equals(bid)) {
                mList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, getItemCount());
                break;
            }
        }
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private class ActivePostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout root;
        private SlidingLayer swipeable;
        private View alphable, delete;
        private ImageView photo;
        private TextView brand, initialPrice, bid;
        private CounterTextView time;

        private int width, offset;

        public ActivePostHolder(View itemView) {
            super(itemView);

            root = (FrameLayout) itemView.findViewById(R.id.root);
            swipeable = (SlidingLayer) itemView.findViewById(R.id.swipeable);
            alphable = itemView.findViewById(R.id.alphable);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            delete = itemView.findViewById(R.id.delete);
            time = (CounterTextView) itemView.findViewById(R.id.time);
            brand = (TextView) itemView.findViewById(R.id.brand);
            initialPrice = (TextView) itemView.findViewById(R.id.initial_price);
            bid = (TextView) itemView.findViewById(R.id.bid);

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

            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            swipeable.openLayer(true);
            mActionListener.onDeleteClick((IncomingBid) root.getTag());
        }
    }

    public interface ParentSwipeListener {
        void onParentSwipeEnable(boolean enable);
    }

    public interface ActionListener {
        void onDeleteClick(IncomingBid bid);
    }

    private class CreatedComparator implements Comparator<IncomingBid> {

        @Override
        public int compare(IncomingBid o1, IncomingBid o2) {
            return ((Long) o2.getCreated()).compareTo(o1.getCreated());
        }
    }
}