package dk.techtify.swipr.dialog.store;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;

/**
 * Created by Pavel on 1/4/2017.
 */

public class BidTimerDialog extends BaseDialog {

    private static final String POSITION_H = "dk.techtify.swipr.dialog.sell.BidTimerDialog.POSITION_H";
    private static final String POSITION_M = "dk.techtify.swipr.dialog.sell.BidTimerDialog.POSITION_M";

    private boolean mCancelBid = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlidingAnimationReverse;

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!mCancelBid) {
            mTimeSelectedListener.onBack();
        }
        super.onDismiss(dialog);
    }

    public static BidTimerDialog newInstance(int defPositionH, int defPositionM) {
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION_H, defPositionH);
        bundle.putInt(POSITION_M, defPositionM);
        BidTimerDialog fragment = new BidTimerDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    private TimeSelectedListener mTimeSelectedListener;

    public void setSizeSelectedListener(TimeSelectedListener sizeSelectedListener) {
        mTimeSelectedListener = sizeSelectedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bid_timer, null);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCancelBid = true;
                getDialog().dismiss();
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        final MaterialNumberPicker hoursPicker = (MaterialNumberPicker) view.findViewById(R.id.hours);
        final MaterialNumberPicker minutesPicker = (MaterialNumberPicker) view.findViewById(R.id.minutes);

        hoursPicker.setWrapSelectorWheel(true);
        hoursPicker.setValue(getArguments().getInt(POSITION_H));
        minutesPicker.setWrapSelectorWheel(true);
        minutesPicker.setValue(getArguments().getInt(POSITION_M));
        minutesPicker.setDisplayedValues(Constants.BID_MINS_ARRAY);

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimeSelectedListener.onTimeSelected(hoursPicker.getValue(), minutesPicker.getValue());
                getDialog().dismiss();
            }
        });

        return view;
    }

    public interface TimeSelectedListener {

        void onTimeSelected(int hoursPosition, int minPosition);

        void onBack();
    }
}