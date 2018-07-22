package dk.techtify.swipr.adapter.store;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.fragment.store.StoreItemHolderFragment;
import dk.techtify.swipr.model.store.Product;

/**
 * Created by Pavel on 12/31/2016.
 */

public class StoreItemHolderAdapter extends FragmentPagerAdapter {

    private List<Product> mItems;
    private boolean mDownloadOtherItems = true;
    private SparseArray<StoreItemHolderFragment> mRegisteredFragments = new SparseArray<>();

    public StoreItemHolderAdapter(FragmentManager fm, boolean downloadOtherItems) {
        super(fm);

        mDownloadOtherItems = downloadOtherItems;
        mItems = new ArrayList<>();
    }

    public StoreItemHolderAdapter(FragmentManager fm) {
        super(fm);

        mDownloadOtherItems = true;
        mItems = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return StoreItemHolderFragment.newInstance(mItems.get(position), mDownloadOtherItems);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        StoreItemHolderFragment fragment = (StoreItemHolderFragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        if (AppConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "Added fragment to position " + position);
        }
        return fragment;
    }

    public void addAll(List<Product> bids) {
        mItems.addAll(bids);
        notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        if (AppConfig.DEBUG) {
            Log.d(getClass().getSimpleName(), "Removed fragment from position " + position);
        }
        super.destroyItem(container, position, object);
    }

    public StoreItemHolderFragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }

    public List<Product> getList() {
        return mItems;
    }
}
