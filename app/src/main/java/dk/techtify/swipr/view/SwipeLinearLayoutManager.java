package dk.techtify.swipr.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by Pavel on 1/19/2017.
 */

public class SwipeLinearLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public SwipeLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
