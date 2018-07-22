package dk.techtify.swipr.dialog.sell;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;

/**
 * Created by Pavel on 1/4/2017.
 */

public class SellPriceDialog extends BaseDialog implements View.OnClickListener {

    private PriceListener mPriceListener;
    private int mDefValue;
    private String mTypedValue;

    private TextView mPrice;

    public void setPriceListener(PriceListener priceListener) {
        mPriceListener = priceListener;
    }

    public void setDefaultValue(int defValue) {
        mDefValue = defValue;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sell_price, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> getDialog().dismiss());

        mTypedValue = String.valueOf(mDefValue);
        mPrice = view.findViewById(R.id.price);
        setPriceToTextView();

        int[] ids = new int[]{R.id.point_1, R.id.point_2, R.id.point_3, R.id.point_4, R.id.point_5,
                R.id.point_6, R.id.point_7, R.id.point_8, R.id.point_9, R.id.point_0, R.id.point_c};
        TextView[] btns = new TextView[11];
        for (int i = 0; i < btns.length; i++) {
            btns[i] = view.findViewById(ids[i]);
            btns[i].setOnClickListener(this);
        }

        view.findViewById(R.id.positive).setOnClickListener(view1 -> {
            if (mTypedValue.length() < 1 || mTypedValue.equals("0")) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.set_price, null);
                return;
            }
            mPriceListener.onPriceSet(Integer.parseInt(mTypedValue));
            getDialog().dismiss();
        });

        return view;
    }

    private void setPriceToTextView() {
        mPrice.setText(TextUtils.concat(mTypedValue, " ", getString(R.string.kr)));
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

    public interface PriceListener {

        void onPriceSet(int price);
    }
}