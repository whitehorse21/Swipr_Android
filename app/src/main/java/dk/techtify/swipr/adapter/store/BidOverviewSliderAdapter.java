package dk.techtify.swipr.adapter.store;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import dk.techtify.swipr.fragment.login.GenderEmptyFragment;
import dk.techtify.swipr.fragment.store.BidOverviewSliderFragment;

/**
 * Created by Pavel on 12/31/2016.
 */

public class BidOverviewSliderAdapter extends FragmentPagerAdapter {

    public BidOverviewSliderAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0 ? new GenderEmptyFragment() : new BidOverviewSliderFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
