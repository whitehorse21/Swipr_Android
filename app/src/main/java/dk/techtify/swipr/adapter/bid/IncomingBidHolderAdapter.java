package dk.techtify.swipr.adapter.bid;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.dialog.bid.IncomingBidDialog;
import dk.techtify.swipr.model.profile.IncomingBid;

/**
 * Created by Pavel on 12/31/2016.
 */

public class IncomingBidHolderAdapter extends FragmentPagerAdapter {

    private List<IncomingBid> mItems;
    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();
    private boolean doNotifyDataSetChangedOnce;

    public IncomingBidHolderAdapter(FragmentManager fm) {
        super(fm);

        mItems = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return IncomingBidDialog.newInstance(mItems.get(position), position, mItems.size());
    }

    @Override
    public int getCount() {
        if (doNotifyDataSetChangedOnce) {
            doNotifyDataSetChangedOnce = false;
            notifyDataSetChanged();
        }

        return mItems.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
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

    public Fragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }

    public void addAllBids(List<IncomingBid> bids) {
        doNotifyDataSetChangedOnce = true;

        mItems.addAll(bids);
        Collections.sort(mItems, new CreatedComparator());
    }

    public void addBid(IncomingBid bid) {
        doNotifyDataSetChangedOnce = true;

        mItems.add(bid);
        Collections.sort(mItems, new CreatedComparator());
    }

    public void removeBid(IncomingBid bid) {
        doNotifyDataSetChangedOnce = true;

        if (mItems.contains(bid)) {
            mItems.remove(bid);
        }
    }

    private class CreatedComparator implements Comparator<IncomingBid> {

        @Override
        public int compare(IncomingBid o1, IncomingBid o2) {
            return ((Long) o2.getCreated()).compareTo(o1.getCreated());
        }
    }
}
