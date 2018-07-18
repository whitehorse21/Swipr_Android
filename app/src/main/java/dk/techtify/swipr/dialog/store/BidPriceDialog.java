package dk.techtify.swipr.dialog.store;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.model.store.OutgoingBid;

/**
 * Created by Pavel on 1/4/2017.
 */

public class BidPriceDialog extends BaseDialog implements View.OnClickListener {

    private static final String REVERSE = "dk.techtify.swipr.dialog.store.BidPriceDialog.REVERSE";

    private OutgoingBid mBid;

    public void setBid(OutgoingBid bid) {
        this.mBid = bid == null ? new OutgoingBid() : bid;
    }

    public static BidPriceDialog newInstance(boolean reverse) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(REVERSE, reverse);
        BidPriceDialog fragment = new BidPriceDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = getArguments().getBoolean(REVERSE) ?
                R.style.DialogSlidingAnimationBackReverse : R.style.DialogSlidingAnimation;

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private ActionListener mActionListener;
    private String mTypedValue;

    private TextView mPrice;

    public void setPriceListener(ActionListener priceListener) {
        mActionListener = priceListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mBid == null) {
            mBid = new OutgoingBid();
        }
        View view = inflater.inflate(R.layout.dialog_bid_price, null);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        mTypedValue = String.valueOf(mBid.getPrice());
        mPrice = (TextView) view.findViewById(R.id.price);
        mPrice.setText(String.valueOf(mBid.getPrice()));
        setPriceToTextView();

        int[] ids = new int[]{R.id.point_1, R.id.point_2, R.id.point_3, R.id.point_4, R.id.point_5,
                R.id.point_6, R.id.point_7, R.id.point_8, R.id.point_9, R.id.point_0, R.id.point_c};
        TextView[] btns = new TextView[11];
        for (int i = 0; i < btns.length; i++) {
            btns[i] = (TextView) view.findViewById(ids[i]);
            btns[i].setOnClickListener(this);
        }

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTypedValue.length() < 1 || mTypedValue.equals("0")) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.set_price, null);
                    return;
                }
                int bid = Integer.parseInt(mTypedValue);
                long price = mBid.getInitialPrice();
                if (bid < (double) price / 2 || bid > (double) price * 1.5) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.bid_cannot_be_less_or_more, null);
                    return;
                }
                mActionListener.onPriceSet(bid);
                getDialog().dismiss();
            }
        });

        TextView timer = (TextView) view.findViewById(R.id.timer);
        if (mBid.getTimerHrsPosition() != 0 || mBid.getTimerMinPosition() != 0) {
            timer.setText(TextUtils.concat(String.valueOf(mBid.getTimerHrsPosition()), " ",
                    mBid.getTimerHrsPosition() == 1 ? getString(R.string.hr) : getString(R.string.hrs),
                    " ", String.valueOf(mBid.getTimerMinPosition() * 5), " ", getString(R.string.min)));
            timer.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.selector_text_white_secondary));
            timer.setBackgroundResource(R.drawable.bck_btn_bid_set_addition_inverse);

        }
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.onTimerClick();
                getDialog().dismiss();
            }
        });

        CheckBox shipping = (CheckBox) view.findViewById(R.id.shipping);
        shipping.setChecked(mBid.isIncludeShipping());
        shipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBid.setIncludeShipping(isChecked);
            }
        });

        TextView message = (TextView) view.findViewById(R.id.message);
        if (mBid.getMessage() != null && mBid.getMessage().length() > 0) {
            message.setText(getString(R.string.message));
            message.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.selector_text_white_secondary));
            message.setBackgroundResource(R.drawable.bck_btn_bid_set_addition_inverse);
        }
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.onMessageClick();
                getDialog().dismiss();
            }
        });

        return view;
    }

    private void setPriceToTextView() {
        mPrice.setText(TextUtils.concat(mTypedValue, " ", getString(R.string.kr)));
        mBid.setPrice(Integer.parseInt(mTypedValue));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.point_c) {
            mTypedValue = "0";
            setPriceToTextView();
        } else if (mTypedValue.length() < 6) {
            if (id == R.id.point_0) {
                if (mTypedValue.length() < 1 || mTypedValue.equals("0")) {
                    return;
                }
                mTypedValue += ((TextView) view).getText().toString();
            } else {
                if (mTypedValue.equals("0")) {
                    mTypedValue = ((TextView) view).getText().toString();
                } else {
                    mTypedValue += ((TextView) view).getText().toString();
                }
            }
            setPriceToTextView();
        }
    }

    public interface ActionListener {

        void onPriceSet(int price);

        void onTimerClick();

        void onMessageClick();
    }
}