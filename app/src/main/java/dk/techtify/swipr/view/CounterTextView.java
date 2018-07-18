package dk.techtify.swipr.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DateTimeHelper;

/**
 * Created by Pavel on 1/25/2017.
 */

public class CounterTextView extends TextView {

    private TimeHandler mTimeHandler;

    private long mTime;

    public CounterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTime(long time) {
        if (time < 1000) {
            setText(getContext().getString(R.string.expired));
            return;
        }
        mTime = time;

        if (mTimeHandler != null) {
            mTimeHandler.removeMessages(0);
        }
        mTimeHandler = new TimeHandler();
        mTimeHandler.sendEmptyMessage(0);
    }

    private class TimeHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                setText(DateTimeHelper.getFormattedCounter(mTime));
                mTime -= 1000;
                sendEmptyMessageDelayed(mTime < 1000 ? 1 : 0, 999);
            } else {
                removeMessages(0);
                setText(getContext().getString(R.string.expired));
            }
        }
    }
}
