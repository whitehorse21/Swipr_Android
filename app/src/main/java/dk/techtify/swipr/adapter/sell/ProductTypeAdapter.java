package dk.techtify.swipr.adapter.sell;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.model.sell.Brand;
import dk.techtify.swipr.model.sell.ProductType;
import dk.techtify.swipr.model.sell.SellProductTypeBrand;

/**
 * Created by Pavel on 1/4/2017.
 */

public class ProductTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<SellProductTypeBrand> mList;
    private OnItemClickListener mOnItemClickListener;

    private String mLocale;

    public ProductTypeAdapter(Context context, List<SellProductTypeBrand> list, String locale,
                              OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mList = new ArrayList<>();
        this.mList.addAll(list);
        this.mOnItemClickListener = onItemClickListener;

        mLocale = locale;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sell_product_type, null);
        RecyclerView.ViewHolder holder = new ProductTypeHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ProductTypeHolder h = (ProductTypeHolder) holder;

        h.root.setTag(mList.get(position));

        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        h.root.setPadding(0, DisplayHelper.dpToPx(mContext, position == 0 ? 12 : 0),
                0, DisplayHelper.dpToPx(mContext, position == mList.size() - 1 ? 12 : 0));

        if (mList.get(position) instanceof ProductType) {
            ProductType data = (ProductType) mList.get(position);
            h.name.setText(mLocale.equals("dk") ? data.getDk() : data.getEn());
        } else {
            Brand data = (Brand) mList.get(position);
            h.name.setText(data.getEn());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<SellProductTypeBrand> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    private class ProductTypeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout root;
        private TextView name;

        public ProductTypeHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            name = itemView.findViewById(R.id.name);
            name.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnItemClickListener.onProductClick((SellProductTypeBrand) root.getTag());
        }
    }

    public interface OnItemClickListener {
        void onProductClick(SellProductTypeBrand product);
    }
}