package dk.techtify.swipr.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.adapter.store.StoreItemHolderAdapter;
import dk.techtify.swipr.asynctask.ApiResponseListener;
import dk.techtify.swipr.asynctask.GetProductsAsyncTask;
import dk.techtify.swipr.fragment.store.StoreItemFragment;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.ActionView;
import dk.techtify.swipr.view.PlusMembershipViewPager;

/**
 * Created by Pavel on 15/11/2016.
 */

public class StoreFragment extends Fragment implements ActionView.ActionClickListener {

    private PlusMembershipViewPager mItemPager;
    private StoreItemHolderAdapter mAdapter;
    private View mBack, mForward, mProgressBar, mEmptyView;

    private boolean mIsPlusMember;

    private InternetStatusReceiver mInternetStatusReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mIsPlusMember = User.getLocalUser().getEmail() != null && User.getLocalUser().isPlusMember();

        ((MainActivity) getActivity()).setContainerTopMargin(false);
        ((MainActivity) getActivity()).getActionView().setTitle("");
        ((MainActivity) getActivity()).getActionView().setActionButton(R.drawable.ic_filter, this);
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), android.R.color.transparent));

        View view = inflater.inflate(R.layout.fragment_store, null);

        mProgressBar = view.findViewById(R.id.progress);
        mEmptyView = view.findViewById(R.id.no_products);
        mEmptyView.setVisibility(View.GONE);
        mBack = view.findViewById(R.id.back);
        mForward = view.findViewById(R.id.forward);

        mBack.setOnClickListener(view12 -> {
            if (mItemPager.getCurrentItem() > 0) {
                mItemPager.setCurrentItem(mItemPager.getCurrentItem() - 1, true);
            }
        });

        mForward.setOnClickListener(view1 -> {
            if (mItemPager.getCurrentItem() < mAdapter.getCount() - 1) {
                mItemPager.setCurrentItem(mItemPager.getCurrentItem() + 1, true);
            }
        });

        if (mAdapter == null) {
            mAdapter = new StoreItemHolderAdapter(getChildFragmentManager());
        }

        mItemPager = view.findViewById(R.id.pager);
        mItemPager.setAdapter(mAdapter);

        final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBack.setVisibility(position == 0 ?
                        View.INVISIBLE : View.VISIBLE);
                mForward.setVisibility(mAdapter.getCount() == 0 || position == mAdapter.getCount() - 1
                        ? View.INVISIBLE : View.VISIBLE);

                if (mAdapter.getCount() == 0) {
                    if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                        getProducts();
                    } else {
                        mInternetStatusReceiver = new InternetStatusReceiver();
                        getActivity().registerReceiver(mInternetStatusReceiver, new IntentFilter(
                                "android.net.conn.CONNECTIVITY_CHANGE"));
                    }
                } else if (position == mAdapter.getCount() - 1 && NetworkHelper.isOnline(getActivity(),
                        NetworkHelper.NONE)) {
                    getProducts();
                }

                if (mAdapter.getCount() > 0) {
                    StoreItemFragment.increaseViewCounter(mAdapter.getList().get(position), true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mItemPager.addOnPageChangeListener(onPageChangeListener);

        mItemPager.post(() -> onPageChangeListener.onPageSelected(mItemPager.getCurrentItem()));

        return view;
    }

    private void getProducts() {
        mProgressBar.setVisibility(View.VISIBLE);
        final String lastId = mAdapter.getList().size() == 0 ? null : mAdapter.getList().get(
                mAdapter.getCount() - 1).getId();
        new GetProductsAsyncTask(lastId, new ApiResponseListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object object) {
                if (StoreFragment.this.isAdded()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    List<Product> newProducts = (List<Product>) object;
                    if (newProducts.size() > 0) {
                        mAdapter.addAll(newProducts);
                    }

                    if (mAdapter.getCount() == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else if (mAdapter.getCount() > 1 && newProducts.size() > 0) {
                        mForward.setVisibility(View.VISIBLE);
                    }

                    if (lastId == null && mAdapter.getCount() > 0 && mAdapter.getList().get(0) != null) {
                        StoreItemFragment.increaseViewCounter(mAdapter.getList().get(0), true);
                    }
                }
            }

            @Override
            public void onError(Object object) {
                if (StoreFragment.this.isAdded()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.error_unknown, null);
                }
            }
        }).execute();
    }

    @Override
    public void onDestroyView() {
        if (mInternetStatusReceiver != null) {
            try {
                getActivity().unregisterReceiver(mInternetStatusReceiver);
            } catch (Exception e) {
                if (AppConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        super.onDestroyView();
    }

    @Override
    public void onActionButtonClick() {

    }

    public void setPlusMemberVisibility(boolean isPlus) {
        mIsPlusMember = isPlus;
    }

    private class InternetStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (AppConfig.DEBUG) {
                        Log.d("NETWORK", "WIFI " + activeNetwork.isConnected());
                    }
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (AppConfig.DEBUG) {
                        Log.d("NETWORK", "MOBILE " + activeNetwork.isConnected());
                    }
                }
                if (mAdapter.getCount() == 0) {
                    getProducts();
                }
            }
        }
    }
}