package dk.techtify.swipr.fragment.login;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;

/**
 * Created by Pavel on 15/11/2016.
 */

public class GenderToggleFragment extends Fragment {

    private TextView mSliderText;
    private ImageView mFemale, mMale;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new FrameLayout(getActivity());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DisplayHelper.dpToPx(getContext(), 64), ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;

        FrameLayout layout = new FrameLayout(getContext());
        layout.setLayoutParams(params);
        layout.setBackgroundResource(R.drawable.bck_btn_create_account);

        mSliderText = new TextView(getContext());
        mSliderText.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mSliderText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mSliderText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.text_xsmall));
        mSliderText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mSliderText.setGravity(Gravity.CENTER);
        mSliderText.setText(getResources().getString(R.string.optional));

        FrameLayout.LayoutParams femaleParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mFemale = new ImageView(getContext());
        mFemale.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mFemale.setLayoutParams(femaleParams);
        mFemale.setImageResource(R.drawable.ic_gender_female_accent);
        mFemale.setAlpha(0f);

        FrameLayout.LayoutParams maleParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        maleParams.gravity = GravityCompat.END;
        mMale = new ImageView(getContext());
        mMale.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mMale.setLayoutParams(maleParams);
        mMale.setImageResource(R.drawable.ic_gender_male_accent);
        mMale.setAlpha(0f);

        layout.addView(mSliderText);
        layout.addView(mFemale);
        layout.addView(mMale);

        ((FrameLayout) view).addView(layout);

        return view;
    }

    public TextView getButton() {
        return mSliderText;
    }

    public View getFemale() {
        return mFemale;
    }

    public View getMale() {
        return mMale;
    }
}
