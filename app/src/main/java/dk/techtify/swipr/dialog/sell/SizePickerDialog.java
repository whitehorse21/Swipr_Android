package dk.techtify.swipr.dialog.sell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;

/**
 * Created by Pavel on 1/4/2017.
 */

public class SizePickerDialog extends BaseDialog {

    private static final String SCALE = "dk.techtify.swipr.dialog.sell.SizePickerDialog.SCALE";
    private static final String POSITION = "dk.techtify.swipr.dialog.sell.SizePickerDialog.POSITION";

    public static SizePickerDialog newInstance(ArrayList<String> scale, int defPosition) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(SCALE, scale);
        bundle.putInt(POSITION, defPosition);
        SizePickerDialog fragment = new SizePickerDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ArrayList<String> mScale;

    private int mMode;

    private SizeSelectedListener mSizeSelectedListener;
    private int mDefValue;

    public void setSizeSelectedListener(SizeSelectedListener sizeSelectedListener) {
        mSizeSelectedListener = sizeSelectedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScale = getArguments().getStringArrayList(SCALE);
        mDefValue = getArguments().getInt(POSITION);
        View view = inflater.inflate(R.layout.dialog_sell_size_picker, null);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        final MaterialNumberPicker picker = (MaterialNumberPicker) view.findViewById(R.id.size);
        picker.setMinValue(0);
        picker.setMaxValue(mScale.size() - 1);
        picker.setDisplayedValues(mScale.toArray(new String[0]));
        picker.setValue(mDefValue);

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSizeSelectedListener.onSizeSelected(picker.getValue(), mScale.get(picker.getValue()));
                getDialog().dismiss();
            }
        });

        return view;
    }

    public interface SizeSelectedListener {

        void onSizeSelected(int position, String size);
    }
}