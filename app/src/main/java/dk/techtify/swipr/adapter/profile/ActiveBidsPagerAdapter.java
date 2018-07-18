package dk.techtify.swipr.adapter.profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.fragment.profile.ActiveBidsFragment;
/**
 * Created by Pavel on 12/31/2016.
 */

public class ActiveBidsPagerAdapter extends FragmentPagerAdapter {

    private SparseArray<ActiveBidsFragment> mRegisteredFragments = new SparseArray<>();

    public ActiveBidsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return ActiveBidsFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ActiveBidsFragment fragment = (ActiveBidsFragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        if (AppConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "Added fragment to position " + position);
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        if (AppConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "Removed fragment from position " + position);
        }
        super.destroyItem(container, position, object);
    }

    public ActiveBidsFragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }
}
