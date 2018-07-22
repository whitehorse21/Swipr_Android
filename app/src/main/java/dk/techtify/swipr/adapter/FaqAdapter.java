package dk.techtify.swipr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.model.Faq;
import io.realm.RealmResults;

/**
 * Created by Pavel on 8/2/2016.
 */
public class FaqAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Faq> mList;

    private String mLocale;
    private List<Integer> mOpenPositions;

    public FaqAdapter(Context context, RealmResults<Faq> collections) {
        this.mContext = context;
        this.mList = collections;

        mLocale = Locale.getDefault().getLanguage();
        mOpenPositions = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, null);
        RecyclerView.ViewHolder holder = new FaqItemHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Faq data = mList.get(position);

        FaqItemHolder h = (FaqItemHolder) holder;

        h.answer.setTag(position);

        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        h.root.setPadding(0, DisplayHelper.dpToPx(mContext, position == 0 ? 24 : 0),
                0, DisplayHelper.dpToPx(mContext, position == mList.size() - 1 ? 24 : 0));

        h.question.setText(mLocale.equals("dk") ? data.getDkQuestion() : data.getEnQuestion());
        h.answer.setText(mLocale.equals("dk") ? data.getDkAnswer() : data.getEnAnswer());

        if (mOpenPositions.contains(position)) {
            h.answer.setVisibility(View.VISIBLE);
        } else {
            h.answer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class FaqItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout root;
        private TextView question;
        private TextView answer;

        public FaqItemHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
            question.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = (Integer) answer.getTag();
            if (mOpenPositions.contains(position)) {
                mOpenPositions.remove((Integer) position);
            } else {
                mOpenPositions.add(position);
            }
            notifyItemChanged(position);
        }
    }
}