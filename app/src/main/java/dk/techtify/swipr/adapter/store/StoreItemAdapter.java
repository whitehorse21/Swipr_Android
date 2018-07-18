package dk.techtify.swipr.adapter.store;

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
import dk.techtify.swipr.fragment.store.StoreItemFragment;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.store.SellerBuyer;

/**
 * Created by Pavel on 12/31/2016.
 */

public class StoreItemAdapter extends FragmentPagerAdapter {

    private List<Product> mProducts;
    private SparseArray<StoreItemFragment> mRegisteredFragments = new SparseArray<>();
    private SellerBuyer mSeller;

    public StoreItemAdapter(FragmentManager fm, Product item, SellerBuyer seller) {
        super(fm);

        mProducts = new ArrayList<>();
        mProducts.add(item);
        mSeller = seller;
    }

    @Override
    public Fragment getItem(int position) {
        return StoreItemFragment.newInstance(position, mProducts.get(position), mSeller);
    }

    @Override
    public int getCount() {
        return mProducts.size();
    }

    public void addAll(List<Product> bids) {
        Collections.sort(bids, new CreatedComparator());
        mProducts.addAll(bids);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        StoreItemFragment fragment = (StoreItemFragment) super.instantiateItem(container, position);
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

    public StoreItemFragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }

    public void setSeller(SellerBuyer seller) {
        mSeller = seller;
        for (int i = 0; i < mRegisteredFragments.size(); i++) {
            mRegisteredFragments.get(mRegisteredFragments.keyAt(i)).setSeller(seller);
        }
    }

    public List<Product> getList() {
        return mProducts;
    }

    private class CreatedComparator implements Comparator<Product> {

        @Override
        public int compare(Product o1, Product o2) {
            return ((Long) o2.getCreated()).compareTo(o1.getCreated());
        }
    }
}
