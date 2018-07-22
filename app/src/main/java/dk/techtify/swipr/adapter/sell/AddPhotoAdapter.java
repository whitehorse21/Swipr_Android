package dk.techtify.swipr.adapter.sell;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
@Deprecated
public class AddPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_LOW = 0;
    public static final int VIEW_HIGH = 1;

    private Context mContext;
    private List<Photo> mList;
    private OnPhotoClickListener mOnClickListener;
    private int mDialogWidth;

    public AddPhotoAdapter(Context context, List<Photo> photos,
                           OnPhotoClickListener onClickListener) {
        this.mContext = context;
        this.mList = photos;
        this.mOnClickListener = onClickListener;
        this.mDialogWidth = DisplayHelper.getScreenResolution(context)[0]
                - DisplayHelper.dpToPx(context, 48);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(viewType == VIEW_LOW
                ? R.layout.item_sell_add_photo_low : R.layout.item_sell_add_photo_high, null);
        RecyclerView.ViewHolder holder = new PhotoHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Photo data = mList.get(position);

        PhotoHolder h = (PhotoHolder) holder;

        h.root.setTag(position);

        h.root.setLayoutParams(new AbsListView.LayoutParams(data.getLocalPath() == null ?
                ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams photoParams = (LinearLayout.LayoutParams) h.photo.getLayoutParams();
        if (position == 0) {
            photoParams.width = photoParams.height = mDialogWidth * 55 / 100;
        } else {
            photoParams.width = photoParams.height = (mDialogWidth -
                    DisplayHelper.dpToPx(mContext, 96)) / 2;
        }
        h.photo.setLayoutParams(photoParams);

        if (data.getLocalPath() == null) {
            h.photo.setBorderColor(ContextCompat.getColor(mContext, android.R.color.transparent));
            h.photo.setImageResource(R.drawable.ic_sell_photo_add);
            h.photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            h.remove.setVisibility(View.INVISIBLE);
        } else {
            if (data.getBitmap() == null) {
                h.photo.setImageDrawable(null);
            } else {
                h.photo.setImageBitmap(data.getBitmap());
            }
            h.photo.setBorderColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            h.photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            h.remove.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_HIGH;
        }
        return VIEW_LOW;
    }

    public void removeItem(int position) {
        int realPhotoCount = 0;
        for (Photo p : mList) {
            if (p.getLocalPath() != null) {
                realPhotoCount += 1;
            }
        }
        if (realPhotoCount == AppConfig.SELL_MAX_ATTACHED_PHOTO_COUNT) {
            mList.add(new Photo());
//            notifyDataSetChanged();
        }
        mList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout root;
        private RoundedImageView photo;
        private ImageButton remove;

        public PhotoHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            photo = itemView.findViewById(R.id.photo);
            remove = itemView.findViewById(R.id.remove);
            photo.setOnClickListener(this);
            remove.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.equals(photo)) {
                if (mList.get((Integer) root.getTag()).getLocalPath() == null) {
                    mOnClickListener.onAddClick();
                }
            } else {
                mOnClickListener.onRemoveClick((Integer) root.getTag());
            }
        }
    }

    public interface OnPhotoClickListener {
        void onRemoveClick(int position);

        void onAddClick();
    }
}