package dk.techtify.swipr.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Pavel on 12/31/2016.
 */

public class PlusMembershipViewPager extends ViewPager {

    private float mStartDragX, mLastDragX;
    private boolean isForwardDirection = true, mIsPlusMember = true;

    public PlusMembershipViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!mIsPlusMember) {
            isForwardDirection = processTouchEvent(e);
            if (!isForwardDirection) {
                e.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (!mIsPlusMember) {
            isForwardDirection = processTouchEvent(e);
            if (!isForwardDirection) {
                e.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    private boolean processTouchEvent(MotionEvent e) {
        boolean direction = true;
        float x = e.getX();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastDragX < x) {
                    direction = false;
                } else if (mStartDragX < x) {
                    // swipe back
                    direction = false;
                } else if (mStartDragX > x) {
                    // swipe forward
                    direction = true;
                }
                break;
        }
        mLastDragX = x;
        return direction;
    }
}
