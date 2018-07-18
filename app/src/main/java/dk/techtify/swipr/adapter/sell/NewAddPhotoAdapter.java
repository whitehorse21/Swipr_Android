package dk.techtify.swipr.adapter.sell;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.model.sell.Photo;

/**
 * Created by Pavel on 8/2/2016.
 */
public class NewAddPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Photo> mList;
    private OnPhotoClickListener mOnClickListener;
    private int mDialogWidth;

    private int width, dp24;

    public NewAddPhotoAdapter(Context context, List<Photo> photos,
                              OnPhotoClickListener onClickListener) {
        this.mContext = context;
        this.mList = photos;
        this.mOnClickListener = onClickListener;
        this.mDialogWidth = DisplayHelper.getScreenResolution(context)[0]
                - DisplayHelper.dpToPx(context, 48);

        width = DisplayHelper.dpToPx(context, 160);
        dp24 = DisplayHelper.dpToPx(context, 20);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_sell_add_photo_new, null);
        RecyclerView.ViewHolder holder = new PhotoHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Photo data = mList.get(position);

        PhotoHolder h = (PhotoHolder) holder;

        h.root.setTag(position);

        h.root.setLayoutParams(new AbsListView.LayoutParams(position != mList.size() - 1 ?
                width - dp24 : width, width));
        h.root.setPadding(dp24, dp24, position != mList.size() - 1 ? 0 : dp24, dp24);

        FrameLayout.LayoutParams photoParams = (FrameLayout.LayoutParams) h.photo.getLayoutParams();

        h.photo.setLayoutParams(photoParams);
        h.photo.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (data.getLocalPath() == null) {
            h.remove.setVisibility(View.INVISIBLE);
        } else {
            h.remove.setVisibility(View.VISIBLE);
            if (data.getBitmap() == null) {
                h.photo.setImageDrawable(null);
            } else {
                h.photo.setImageBitmap(data.getBitmap());
            }
        }
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

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout root;
        private RoundedImageView photo;
        private ImageButton remove;

        public PhotoHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView.findViewById(R.id.root);
            photo = (RoundedImageView) itemView.findViewById(R.id.photo);
            remove = (ImageButton) itemView.findViewById(R.id.remove);
            remove.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onRemoveClick((Integer) root.getTag());
        }
    }

    public interface OnPhotoClickListener {
        void onRemoveClick(int position);
    }
}