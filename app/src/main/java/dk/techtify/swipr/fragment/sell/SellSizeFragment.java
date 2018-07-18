package dk.techtify.swipr.fragment.sell;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.sell.SizePickerDialog;
import dk.techtify.swipr.fragment.main.SellFragment;
import dk.techtify.swipr.helper.FragmentHelper;
import dk.techtify.swipr.model.sell.Product;

/**
 * Created by Pavel on 1/12/2017.
 */

public class SellSizeFragment extends Fragment implements SizePickerDialog.SizeSelectedListener {

    private static final String SCALE = "dk.techtify.swipr.fragment.sell.SellSizeFragment.SCALE";
    private static final String POSITION = "dk.techtify.swipr.fragment.sell.SellSizeFragment.POSITION";

    private Product mProduct;
    private ArrayList<String> mScale;
    private int mPosition;

    private ViewPager mViewPager;

    private TextView mSizeTv;

    private int mSizePosition;
    private String mSizeText;

    public int getSizePosition() {
        return mSizePosition;
    }

    public String getSizeText() {
        return mSizeText;
    }

    public TextView getSizeTv() {
        return mSizeTv;
    }

    public static SellSizeFragment newInstance(int position, ArrayList<String> scale) {
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        bundle.putStringArrayList(SCALE, scale);
        SellSizeFragment fragment = new SellSizeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mProduct = ((SellFragment) FragmentHelper.getCurrentFragment((AppCompatActivity) getActivity()))
                .getProduct();
        mViewPager = ((SellFragment) FragmentHelper.getCurrentFragment((AppCompatActivity) getActivity()))
                .getSizePager();
        mPosition = getArguments().getInt(POSITION);
        mScale = getArguments().getStringArrayList(SCALE);

        final View view = inflater.inflate(R.layout.fragment_sell_size, null);

        mSizeTv = (TextView) view.findViewById(R.id.size);
        mSizeTv.setHint(mScale.get(mScale.size() / 2));
        if (mProduct.getSize() != null && mPosition == mProduct.getSizeScalePosition()) {
            onSizeSelected(mProduct.getSizePosition(), mProduct.getSize());
        }
        mSizeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == mPosition) {
                    SizePickerDialog spd = SizePickerDialog.newInstance(mScale,
                            mProduct.getSizePosition() > -1 && mProduct.getSizeScalePosition() ==
                                    mPosition ? mProduct.getSizePosition() : mScale
                                    .size() / 2);
                    spd.setSizeSelectedListener(SellSizeFragment.this);
                    spd.show(getActivity().getSupportFragmentManager(), spd.getClass().getSimpleName());
                } else {
                    mViewPager.setCurrentItem(mPosition);
                }
            }
        });

        return view;
    }

    @Override
    public void onSizeSelected(int position, String size) {
        mSizePosition = position;
        mSizeText = size;

        mProduct.setSize(size);
        mProduct.setSizePosition(position);
        mProduct.setSizeScalePosition(mPosition);

        mSizeTv.setText(size);
        mSizeTv.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.selector_text_white_primary));
        mSizeTv.setBackgroundResource(R.drawable.bck_sell_size_inverse);
    }
}
