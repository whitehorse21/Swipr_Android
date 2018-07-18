package dk.techtify.swipr.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;

/**
 * Created by Pavel on 1/3/2017.
 */

public class RatingBar extends FrameLayout {

    private static int sWidth;

    public RatingBar(Context context) {
        super(context);

        if (sWidth == 0) {
            sWidth = DisplayHelper.dpToPx(context, 19);
        }

        addLayouts();
    }

    public RatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (sWidth == 0) {
            sWidth = DisplayHelper.dpToPx(context, 19);
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        addLayouts();
    }

    private void addLayouts() {
        for (int i = 0; i < 2; i++) {
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(ll);
        }

        for (int i = 0; i < 5; i++) {
            ImageView v = new ImageView(getContext());
            v.setImageResource(R.drawable.ic_rating_star_empty);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                    .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.leftMargin = DisplayHelper.dpToPx(getContext(), 8);
            }
            v.setLayoutParams(params);
            ((ViewGroup) getChildAt(0)).addView(v);
        }
    }

    public void setRating(float userRating) {
        if (userRating > 5f) {
            userRating = 5f;
        } else if (userRating < 0f) {
            userRating = 0f;
        }

        ((ViewGroup) getChildAt(1)).removeAllViews();

        if (userRating == 0f) {
            return;
        }

        if (userRating < 1f) {
            addPartOfRedStar(1f - userRating);
        } else if (userRating < 2f) {
            addRedStars(1);
            if (userRating > 1f) {
                addPartOfRedStar(2f - userRating);
            }
        } else if (userRating < 3f) {
            addRedStars(2);
            if (userRating > 2f) {
                addPartOfRedStar(3f - userRating);
            }
        } else if (userRating < 4f) {
            addRedStars(3);
            if (userRating > 3f) {
                addPartOfRedStar(4f - userRating);
            }
        } else if (userRating < 5f) {
            addRedStars(4);
            if (userRating > 4f) {
                addPartOfRedStar(5f - userRating);
            }
        } else {
            addRedStars(5);
        }
    }

    private void addPartOfRedStar(float rating) {
        ImageView v = new ImageView(getContext());
        v.setScaleType(ImageView.ScaleType.MATRIX);
        v.setImageResource(R.drawable.ic_rating_star_full);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (sWidth - (
                sWidth * rating)), ViewGroup.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(params);
        ((ViewGroup) getChildAt(1)).addView(v);
    }

    private void addRedStars(int number) {
        for (int i = 0; i < number; i++) {
            ImageView v = new ImageView(getContext());
            v.setImageResource(R.drawable.ic_rating_star_full);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                    .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < 4) {
                params.rightMargin = DisplayHelper.dpToPx(getContext(), 8);
            }
            v.setLayoutParams(params);
            ((ViewGroup) getChildAt(1)).addView(v);
        }
    }
}
