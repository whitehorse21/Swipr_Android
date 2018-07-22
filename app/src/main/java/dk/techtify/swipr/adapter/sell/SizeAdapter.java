package dk.techtify.swipr.adapter.sell;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.fragment.sell.SellSizeFragment;

/**
 * Created by Pavel on 12/31/2016.
 */

public class SizeAdapter extends FragmentPagerAdapter {

    private FragmentManager mFm;
    private ArrayList<ArrayList<String>> mScales;
    private SparseArray<SellSizeFragment> mRegisteredFragments = new SparseArray<>();

    public SizeAdapter(FragmentManager fm, Map<String, ArrayList<String>> allScales,
                       ArrayList<String> scales) {
        super(fm);
        mFm = fm;
        mScales = new ArrayList<>();

        for (Object o : allScales.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            if (scales.contains(pair.getKey())) {
                mScales.add((ArrayList<String>) pair.getValue());
            }
        }
    }

    public void clearAll() {
        for (int i = 0; i < mRegisteredFragments.size(); i++) {
            mFm.beginTransaction().remove(mRegisteredFragments.get(i)).commit();
        }
        mRegisteredFragments.clear();
    }

    @Override
    public Fragment getItem(int position) {
        return SellSizeFragment.newInstance(position, mScales.get(position));
    }

    @Override
    public int getCount() {
        return mScales.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SellSizeFragment fragment = (SellSizeFragment) super.instantiateItem(container, position);
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

    public SellSizeFragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }
}
