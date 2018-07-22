package dk.techtify.swipr.view;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;

/**
 * Created by Pavel on 1/7/2017.
 */

public class PlusHandView extends FrameLayout {

    private final int MAX_DRAG;

    private ImageView mImageHand, mImageShadow, mBoom;

    private int mInitialHeight;
    private float mStartDragY, mLastDragY, mMinDragY;
    private boolean mAllowDrag = false;

    private SoundPool mSoundPool;
    private int mBoomAudio;

    private static int s5dp;

    private AnimationDoneListener mAnimationDoneListener;

    public void setAnimationDoneListener(AnimationDoneListener animationDoneListener) {
        this.mAnimationDoneListener = animationDoneListener;
    }

    public PlusHandView(Context context, AttributeSet attrs) {
        super(context, attrs);

        MAX_DRAG = DisplayHelper.dpToPx(context, 100);

        s5dp = DisplayHelper.dpToPx(getContext(), 5);

        setPadding(0, 0, 0, 0);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        mImageShadow = new ImageView(getContext());
        mImageShadow.setAdjustViewBounds(true);
        mImageShadow.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageShadow.setImageResource(R.drawable.ic_plus_hand_shadow);

        mImageHand = new ImageView(getContext());
        mImageHand.setAdjustViewBounds(true);
        mImageHand.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageHand.setImageResource(R.drawable.ic_plus_hand);

        mBoom = new ImageView(getContext());
        mBoom.setAlpha(0.1f);
        mBoom.setImageResource(R.drawable.bck_plus_boom);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mBoomAudio = mSoundPool.load(getContext(), R.raw.deal, 2);
    }

    public void setHandParams(int height) {
        mInitialHeight = height;

        LayoutParams boomParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        boomParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        boomParams.bottomMargin = height + DisplayHelper.dpToPx(getContext(), 24);
        mBoom.setLayoutParams(boomParams);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.bottomMargin = DisplayHelper.dpToPx(getContext(), 24);

        mImageShadow.setLayoutParams(params);
        mImageHand.setLayoutParams(params);

        addView(mImageShadow);
        addView(mImageHand);

        mImageHand.setOnTouchListener(mHandTouchListener);
    }

    View.OnTouchListener mHandTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && !mAllowDrag) {
                return false;
            }
            boolean isTopDirection = processTouchEvent(motionEvent);

//            if (motionEvent.getY() < mStartDragY - MAX_DRAG) {
            if (mImageHand.getScaleY() > 2f) {
                mAllowDrag = false;
                doneAnimation();
                return false;
            }

            if (!isTopDirection) {
                mAllowDrag = false;
                motionEvent.setAction(MotionEvent.ACTION_CANCEL);
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                mImageShadow.animate().translationY(0);
                mImageShadow.animate().translationX(0);
                mImageShadow.animate().scaleX(1f).scaleY(1f);

                mImageHand.animate().translationY(0);
                mImageHand.animate().scaleX(1f).scaleY(1f);
            } else if (mLastDragY - mStartDragY < mImageHand.getTranslationY()) {
                mImageShadow.setTranslationY((mLastDragY - mStartDragY) / 1.35f);
                mImageShadow.setTranslationX(-(mLastDragY - mStartDragY) / 15f);
                mImageShadow.setScaleX(calculateScale());
                mImageShadow.setScaleY(calculateScale());

                mImageHand.setTranslationY((mLastDragY - mStartDragY) / 1f);
                mImageHand.setScaleX(calculateScale());
                mImageHand.setScaleY(calculateScale());
            }
            return motionEvent.getAction() != MotionEvent.ACTION_CANCEL;
        }
    };

    private void doneAnimation() {
        addView(mBoom);

        final float scale = mImageHand.getScaleX();
        mBoom.animate().scaleX(7f).scaleY(7f).alpha(0);
        mImageShadow.animate().scaleX(scale * 0.3f).scaleY(scale * 0.3f);
        mImageHand.animate().scaleX(scale * 0.3f).scaleY(scale * 0.3f);
        float volume = (float) ((AudioManager) getContext().getSystemService(Activity.AUDIO_SERVICE)).
                getStreamVolume(AudioManager.STREAM_MUSIC);
        mSoundPool.play(mBoomAudio, volume, volume, 1, 0, 1f);

        mImageHand.postDelayed(() -> {
            mImageShadow.animate().scaleX(scale).scaleY(scale);
            mImageHand.animate().scaleX(scale).scaleY(scale);
        }, 100);
        mImageHand.postDelayed(() -> {
            mImageShadow.animate().translationY(0).scaleX(1f).scaleY(1f);
            mImageHand.animate().translationY(0).scaleX(1f).scaleY(1f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mAnimationDoneListener.onAnimationDone();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    mAnimationDoneListener.onAnimationDone();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }, 1000);
    }

    private float calculateScale() {
        float total = MAX_DRAG * 1.2f;
        float lastDragAbsolute = mStartDragY - mLastDragY;
        return 1 + (lastDragAbsolute / total);
    }

    private boolean processTouchEvent(MotionEvent e) {
        boolean direction = true;
        float y = e.getY();
        if (y < mMinDragY) {
            mMinDragY = y;
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mAllowDrag = true;

                mStartDragY = y;
                mMinDragY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                direction = mStartDragY >= y && y <= mMinDragY + s5dp;
//                Log.d("AAA", direction + " " + y + " " + mStartDragY + " " + mMinDragY);
                break;
            case MotionEvent.ACTION_UP:
                mAllowDrag = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                mAllowDrag = false;
                break;
        }
        mLastDragY = y;

        return mAllowDrag && direction;
    }

    public interface AnimationDoneListener {
        void onAnimationDone();
    }
}
