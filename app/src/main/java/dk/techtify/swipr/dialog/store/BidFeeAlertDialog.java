package dk.techtify.swipr.dialog.store;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;

/**
 * Created by Pavel on 1/4/2017.
 */

public class BidFeeAlertDialog extends BaseDialog {

    private View.OnClickListener mNextListener;

    public void setNextListener(View.OnClickListener mNextListener) {
        this.mNextListener = mNextListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bid_fee_alert, null);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        SpannableString ss = new SpannableString("");
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimary)), 0,
                ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView) view.findViewById(R.id.text)).setText(TextUtils.concat(getString(
                R.string.you_will_be_charged), " ", ss, " ", getString(R.string.in_transaction_fee)));

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNextListener.onClick(view);
                getDialog().dismiss();
            }
        });

        return view;
    }
}