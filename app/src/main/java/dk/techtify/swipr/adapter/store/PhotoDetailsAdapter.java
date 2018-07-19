package dk.techtify.swipr.adapter.store;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.GlideApp;

/**
 * Created by Pavel on 1/4/2017.
 */

public class PhotoDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final FirebaseStorage mStorage;

    private Context mContext;
    private List<String> mList;
    private View.OnClickListener mOnClickListener;

    private int mWidth;

    public PhotoDetailsAdapter(Context context, List<String> list,
                               View.OnClickListener onClickListener) {
        this.mContext = context;
        this.mList = new ArrayList<>();
        this.mList.addAll(list);
        this.mOnClickListener = onClickListener;

        mStorage = FirebaseStorage.getInstance();
        mWidth = DisplayHelper.getScreenResolution(context)[0] / 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_details, null);
        RecyclerView.ViewHolder holder = new ProductTypeHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ProductTypeHolder h = (ProductTypeHolder) holder;

        h.root.setLayoutParams(new AbsListView.LayoutParams(position == 0 ? mWidth : mWidth + DisplayHelper.dpToPx(mContext, 8), ViewGroup.LayoutParams.MATCH_PARENT));
        h.root.setPadding(DisplayHelper.dpToPx(mContext, position == 0 ? 0 : 8), 0, 0, 0);

        h.photo.setScaleType(ImageView.ScaleType.CENTER_CROP);

        h.root.setTag(position);
        if (!TextUtils.isEmpty(mList.get(position))) {
            GlideApp.with(mContext)
                    .load(mStorage.getReferenceFromUrl(mList.get(position)))
                    .into(h.photo);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class ProductTypeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout root;
        private ImageView photo;

        ProductTypeHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView.findViewById(R.id.root);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            photo.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onClick(root);
        }
    }
}