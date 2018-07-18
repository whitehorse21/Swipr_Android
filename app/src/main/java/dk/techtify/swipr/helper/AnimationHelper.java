package dk.techtify.swipr.helper;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by Pavel on 4/5/2016.
 */
public class AnimationHelper {

    public static void showView(int duration, final OnFinishListener listener, final View... views) {
        Animation alpha = new AlphaAnimation(0, 1);
        alpha.setDuration(duration);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                for (View view : views) {
                    view.setVisibility(View.VISIBLE);
                }
                if (listener != null) {
                    listener.onAnimationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        for (View view : views) {
            if (view != null)
                view.startAnimation(alpha);
        }
    }

    public static void hideView(final View... views) {
        Animation alpha = new AlphaAnimation(1, 0);
        alpha.setDuration(300);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                for (View view : views)
                    view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        for (View view : views) {
            if (view != null)
                view.startAnimation(alpha);
        }
    }

    public interface OnFinishListener {
        void onAnimationFinished();
    }
}
