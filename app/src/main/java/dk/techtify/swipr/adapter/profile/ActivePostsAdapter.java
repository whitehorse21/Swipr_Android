package dk.techtify.swipr.adapter.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.List;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.model.store.Product;

/**
 * Created by Pavel on 1/4/2017.
 */

public class ActivePostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Product> mList;

    private FirebaseStorage mStorage;

    private ParentSwipeListener mParentSwipeListener;
    private ActionListener mActionListener;

    public ActivePostsAdapter(Context context, List<Product> list, ParentSwipeListener
            parentSwipeListener, ActionListener actionListener) {
        this.mContext = context;
        this.mList = list;

        mStorage = FirebaseStorage.getInstance();

        mParentSwipeListener = parentSwipeListener;
        mActionListener = actionListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_posts, null);
        RecyclerView.ViewHolder holder = new ActivePostHolder(layoutView);
        return holder;
    }

    @SuppressWarnings("NumericOverflow")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Product product = mList.get(position);

        ActivePostHolder h = (ActivePostHolder) holder;
        h.root.setTag(position);
        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayHelper.dpToPx(mContext, 112)));

        if (product.getPhotos() != null && product.getPhotos().size() > 0 && !TextUtils.isEmpty(product.getPhotos().get(0))) {
            GlideApp.with(mContext)
                    .load(mStorage.getReferenceFromUrl(product.getPhotos().get(0)))
                    .into(h.photo);
        } else {
            h.photo.setImageDrawable(null);
        }

        h.brand.setText(product.getBrand().getName());
        h.views.setText(String.valueOf(product.getViews()));
        h.price.setText(TextUtils.concat(String.valueOf(product.getPrice()), " ", mContext.getString(R.string.kr)));

        h.created.setText(DateTimeHelper.getFormattedDate(product.getCreated(), "dd.MM.yyyy"));
        h.until.setText(DateTimeHelper.getFormattedDate(product.getCreated() +
                Constants.DAY_IN_MILLIS * 10, "dd.MM.yyyy"));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private class ActivePostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout root;
        private SlidingLayer swipeable;
        private View alphable, share, delete;
        private ImageView photo;
        private TextView brand, views, price, until, created;

        private int width, offset;

        public ActivePostHolder(View itemView) {
            super(itemView);

            root = (FrameLayout) itemView.findViewById(R.id.root);
            swipeable = (SlidingLayer) itemView.findViewById(R.id.swipeable);
            alphable = itemView.findViewById(R.id.alphable);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            share = itemView.findViewById(R.id.share);
            delete = itemView.findViewById(R.id.delete);
            until = (TextView) itemView.findViewById(R.id.until);
            created = (TextView) itemView.findViewById(R.id.created);
            brand = (TextView) itemView.findViewById(R.id.brand);
            views = (TextView) itemView.findViewById(R.id.views);
            price = (TextView) itemView.findViewById(R.id.price);

            width = DisplayHelper.getScreenResolution(mContext)[0];
            offset = width - DisplayHelper.dpToPx(mContext, 192);

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

            share.setOnClickListener(this);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            swipeable.openLayer(true);
            if (v.getId() == R.id.share) {
                mActionListener.onShare((Integer) root.getTag());
            } else {
                mActionListener.onDelete((Integer) root.getTag());
            }
        }
    }

    public interface ParentSwipeListener {
        void onParentSwipeEnable(boolean enable);
    }

    public interface ActionListener {
        void onShare(int position);

        void onDelete(int position);
    }
}