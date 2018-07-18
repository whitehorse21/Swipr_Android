package dk.techtify.swipr.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import dk.techtify.swipr.R;
import dk.techtify.swipr.fragment.login.GenderEmptyFragment;
import dk.techtify.swipr.fragment.login.GenderToggleFragment;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.FragmentHelper;

/**
 * Created by Pavel on 12/12/2016.
 */

public class GenderSelectorView extends FrameLayout {

    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    private int mOffset;

    private GenderToggleFragment mGenderToggleFragment;
    private ViewPager mViewPager;
    private ImageView mFemale, mMale;

    public GenderSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mOffset = DisplayHelper.getScreenResolution(getContext())[0] / 2;
    }

    public int getGender() {
        int gender = mViewPager.getCurrentItem() == 1 ? GENDER_UNKNOWN :
                (mViewPager.getCurrentItem() == 0 ? GENDER_MALE : GENDER_FEMALE);
        return gender;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        mGenderToggleFragment = new GenderToggleFragment();

        mViewPager = new ViewPager(getContext());
        mViewPager.setId(R.id.gender_selector_view_id);
        mViewPager.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mViewPager.setAdapter(new PositionAdapter(FragmentHelper.getCurrentFragment(((
                AppCompatActivity) getContext())).getChildFragmentManager()));
        mViewPager.setCurrentItem(1, false);
        mViewPager.setPageMargin(-mOffset);
//        mViewPager.setPageTransformer(true, new FlipPageViewTransform());

        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayHelper.dpToPx(getContext(), 64)));
        setBackgroundResource(R.drawable.bck_editable_light);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && positionOffset == 0.0) {
                    mGenderToggleFragment.getButton().setAlpha(1f);
                } else if ((position == 1 && positionOffset < 0.2) ||
                        (position == 0 && positionOffset > 0.8)) {
                    float alpha;
                    if (position == 1) {
                        alpha = 1 - (positionOffset * 5);
                    } else {
                        alpha = 1 - ((1 - positionOffset) * 5);
                    }
                    mGenderToggleFragment.getButton().setAlpha(alpha);
                } else {
                    mGenderToggleFragment.getButton().setAlpha(0f);
                }

                if (position == 2) {
                    mGenderToggleFragment.getFemale().setAlpha(1f);
                    mGenderToggleFragment.getMale().setAlpha(0f);
                } else if (position == 1 && positionOffset > 0.9) {
                    mGenderToggleFragment.getFemale().setAlpha(1 - ((1 - positionOffset) * 10));
                    mGenderToggleFragment.getMale().setAlpha(0f);
                } else if (position == 0 && positionOffset < 0.1) {
                    mGenderToggleFragment.getFemale().setAlpha(0f);
                    mGenderToggleFragment.getMale().setAlpha(1 - (positionOffset * 10));
                } else {
                    mGenderToggleFragment.getFemale().setAlpha(0f);
                    mGenderToggleFragment.getMale().setAlpha(0f);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        LayoutParams femaleParams = new LayoutParams(DisplayHelper.dpToPx(getContext(), 64), ViewGroup.LayoutParams.MATCH_PARENT);
        mFemale = new ImageView(getContext());
        mFemale.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mFemale.setLayoutParams(femaleParams);
        mFemale.setImageResource(R.drawable.ic_gender_female);

        LayoutParams maleParams = new LayoutParams(DisplayHelper.dpToPx(getContext(), 64), ViewGroup.LayoutParams.MATCH_PARENT);
        maleParams.gravity = GravityCompat.END;
        mMale = new ImageView(getContext());
        mMale.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mMale.setLayoutParams(maleParams);
        mMale.setImageResource(R.drawable.ic_gender_male);

        addView(mFemale);
        addView(mMale);
        addView(mViewPager);
    }

    private class PositionAdapter extends FragmentPagerAdapter {

        public PositionAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 || position == 2) {
                return new GenderEmptyFragment();
            } else {
                return mGenderToggleFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
