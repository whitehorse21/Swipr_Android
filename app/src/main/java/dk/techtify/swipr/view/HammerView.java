package dk.techtify.swipr.view;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;

/**
 * Created by Pavel on 1/31/2017.
 */

public class HammerView extends FrameLayout {

    private static final int DIRECTION_FORWARD = 0;
    private static final int DIRECTION_BACK = 1;

    private ImageView mImage;
    private DialView mDialView;

    private int mWidth;

    private int mAbsolutePosition = 0, mCurrentPosition = 0;
    private boolean shouldChangeDirection;
    private int mDirection;

    private SoundPool mSoundPool;
    private int mBoomAudio;

    private OnBidAcceptedListener mOnBidAcceptedListener;
    private boolean hasFinished = false;
    private static Handler handler;

    public void setOnBidAcceptedListener(OnBidAcceptedListener onBidAcceptedListener) {
        this.mOnBidAcceptedListener = onBidAcceptedListener;
    }

    private int[] images = new int[]{R.drawable.bid_bg_00000, R.drawable.bid_bg_00001, R.drawable.bid_bg_00002,
//            R.drawable.bid_bg_00003, R.drawable.bid_bg_00004, R.drawable.bid_bg_00005,
//            R.drawable.bid_bg_00006, R.drawable.bid_bg_00007, R.drawable.bid_bg_00008,
            R.drawable.bid_bg_00009, R.drawable.bid_bg_00010, R.drawable.bid_bg_00011,
            R.drawable.bid_bg_00012, R.drawable.bid_bg_00013, R.drawable.bid_bg_00014,
            R.drawable.bid_bg_00015, R.drawable.bid_bg_00016, R.drawable.bid_bg_00017,
            R.drawable.bid_bg_00018, R.drawable.bid_bg_00019, R.drawable.bid_bg_00020,
            R.drawable.bid_bg_00021, R.drawable.bid_bg_00022, R.drawable.bid_bg_00023,
            R.drawable.bid_bg_00024, R.drawable.bid_bg_00025, R.drawable.bid_bg_00026,
            R.drawable.bid_bg_00027, R.drawable.bid_bg_00028, R.drawable.bid_bg_00029,
            R.drawable.bid_bg_00030, R.drawable.bid_bg_00031, R.drawable.bid_bg_00032,
            R.drawable.bid_bg_00033, R.drawable.bid_bg_00034, R.drawable.bid_bg_00035,
            R.drawable.bid_bg_00036, R.drawable.bid_bg_00037, R.drawable.bid_bg_00038,
            R.drawable.bid_bg_00039, R.drawable.bid_bg_00040, R.drawable.bid_bg_00041,
            R.drawable.bid_bg_00042, R.drawable.bid_bg_00043, R.drawable.bid_bg_00044,
            R.drawable.bid_bg_00045, R.drawable.bid_bg_00046, R.drawable.bid_bg_00047,
            R.drawable.bid_bg_00048, R.drawable.bid_bg_00049, R.drawable.bid_bg_00050,
            R.drawable.bid_bg_00051, R.drawable.bid_bg_00052, R.drawable.bid_bg_00053,
            R.drawable.bid_bg_00054, R.drawable.bid_bg_00055};

    public HammerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mWidth = DisplayHelper.getScreenResolution(context)[0] - DisplayHelper.dpToPx(context, 48);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        mImage = findViewById(R.id.image);

        mDialView = new DialView(getContext()) {
            @Override
            protected void onRotate(int offset) {
                mAbsolutePosition += offset;

                if (mDirection == DIRECTION_BACK) {
                    offset = -offset;
                }

                if (mCurrentPosition + offset < 0 || mCurrentPosition + offset > images.length - 1) {
                    return;
                }

                if (mDirection == DIRECTION_FORWARD && mAbsolutePosition > 14) {
                    mCurrentPosition = 14;
                    mImage.setImageResource(images[14]);
                    shouldChangeDirection = true;
//                } else if (mDirection == DIRECTION_BACK && mAbsolutePosition < 14) {
//                    mCurrentPosition = 14;
//                    mImage.setImageResource(images[14]);
//                    shouldChangeDirection = true;
                } else if (mCurrentPosition >= 18) {
                    hasFinished = true;
                    mDialView.setEnabled(false);
                    finishHandler();
                } else {
                    if (shouldChangeDirection) {
                        if (mDirection == DIRECTION_FORWARD) {
                            mDirection = DIRECTION_BACK;
                        } else {
                            mDirection = DIRECTION_FORWARD;
                        }
                        shouldChangeDirection = false;
                        offset = -offset;
                    }
                    mCurrentPosition += offset;
                    mImage.setImageResource(images[mCurrentPosition]);
                }

//                Log.d("AAA", offset + " " + mCurrentPosition + " " + mAbsolutePosition + " direction: " + mDirection);
            }
        };
        mDialView.setStepAngle(3f);
        mDialView.setDiscArea(.0f, .90f);
        mDialView.setAlpha(0.0f);

        LayoutParams dialParams = new LayoutParams(mWidth * 100 / 80, mWidth * 100 / 80);
        dialParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        dialParams.bottomMargin = (int) ((float) -mWidth / 1.5);
        dialParams.rightMargin = (int) ((float) -mWidth / 3.1);
        mDialView.setLayoutParams(dialParams);
        addView(mDialView);

        mDialView.setOnStartStopListener(new DialView.OnStartStopListener() {
            @Override
            public void onStart() {
                setDefault();
            }

            @Override
            public void onStop() {
                if (mDirection == DIRECTION_FORWARD || !hasFinished) {
                    returnToStartHandler();
                }
            }
        });
        if (!isInEditMode()) {
            mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            mBoomAudio = mSoundPool.load(getContext(), R.raw.deal, 2);
        }
    }

    private void finishHandler() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    if (mCurrentPosition < images.length - 2) {
                        mCurrentPosition += 1;
                        mImage.setImageResource(images[mCurrentPosition]);

                        if (mCurrentPosition == 22) {
                            float volume = (float) ((AudioManager) getContext().getSystemService(Activity.AUDIO_SERVICE)).
                                    getStreamVolume(AudioManager.STREAM_MUSIC);
                            mSoundPool.play(mBoomAudio, volume, volume, 1, 0, 1f);
                        }
                        sendEmptyMessage(0);
                    } else {
                        sendEmptyMessage(1);
                    }
                } else {
                    removeMessages(0);
                    if (mOnBidAcceptedListener != null) {
                        mOnBidAcceptedListener.onBidAccepted();
                    }
                }
            }
        };
        handler.sendEmptyMessage(0);
    }

    private void returnToStartHandler() {
        new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    if (mCurrentPosition > 0) {
                        mCurrentPosition -= 1;
                        mImage.setImageResource(images[mCurrentPosition]);
                        sendEmptyMessage(0);
                    } else {
                        sendEmptyMessage(1);
                    }
                } else {
                    removeMessages(0);
                }
            }
        }.sendEmptyMessage(0);
    }

    public void setDefault() {
        mCurrentPosition = 0;
        mAbsolutePosition = 0;
        hasFinished = false;
        mDialView.setEnabled(true);
        shouldChangeDirection = false;
        mDirection = DIRECTION_FORWARD;
        mImage.setImageResource(images[mCurrentPosition]);
    }

    public interface OnBidAcceptedListener {
        void onBidAccepted();
    }
}
