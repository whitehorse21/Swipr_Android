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
import dk.techtify.swipr.asynctask.GetFavouritesAsyncTask;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 15/11/2016.
 */

public class FavouritesFragment extends Fragment implements ActionView.ActionClickListener {

    private ViewPager mItemPager;
    private StoreItemHolderAdapter mAdapter;
    private View mBack, mForward, mProgressBar, mEmptyView;

    private InternetStatusReceiver mInternetStatusReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setContainerTopMargin(false);
        ((MainActivity) getActivity()).getActionView().setActionButton(R.drawable.ic_close, this);
        ((MainActivity) getActivity()).getActionView().setTitle("");
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), android.R.color.transparent));

        View view = inflater.inflate(R.layout.fragment_favourites, null);

        mProgressBar = view.findViewById(R.id.progress);
        mEmptyView = view.findViewById(R.id.no_products);
        mEmptyView.setVisibility(View.GONE);
        mBack = view.findViewById(R.id.back);
        mForward = view.findViewById(R.id.forward);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemPager.getCurrentItem() > 0) {
                    mItemPager.setCurrentItem(mItemPager.getCurrentItem() - 1, true);
                }
            }
        });

        mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemPager.getCurrentItem() < mAdapter.getCount() - 1) {
                    mItemPager.setCurrentItem(mItemPager.getCurrentItem() + 1, true);
                }
            }
        });

        if (mAdapter == null) {
            mAdapter = new StoreItemHolderAdapter(getChildFragmentManager(), false);
        }

        mItemPager = (ViewPager) view.findViewById(R.id.pager);
        mItemPager.setAdapter(mAdapter);
        mItemPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBack.setVisibility(position == 0 ?
                        View.INVISIBLE : View.VISIBLE);
                mForward.setVisibility(mAdapter.getCount() == 0 || position == mAdapter.getCount() - 1
                        ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mAdapter.getCount() == 0) {
            mBack.setVisibility(View.INVISIBLE);
            mForward.setVisibility(View.INVISIBLE);
        } else {
            mBack.setVisibility(mItemPager.getCurrentItem() == 0 ?
                    View.INVISIBLE : View.VISIBLE);
            mForward.setVisibility(mItemPager.getCurrentItem() == mAdapter.getCount() - 1
                    ? View.INVISIBLE : View.VISIBLE);
        }

        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
            getProducts();
        } else {
            mInternetStatusReceiver = new InternetStatusReceiver();
            getActivity().registerReceiver(mInternetStatusReceiver, new IntentFilter(
                    "android.net.conn.CONNECTIVITY_CHANGE"));
        }

        return view;
    }

    private void getProducts() {
        mProgressBar.setVisibility(View.VISIBLE);
        final String lastId = mAdapter.getList().size() == 0 ? null : mAdapter.getList().get(
                mAdapter.getCount() - 1).getId();
        new GetFavouritesAsyncTask(lastId, new ApiResponseListener() {
            @Override
            public void onSuccess(Object object) {
                if (FavouritesFragment.this.isAdded()) {
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
                }
            }

            @Override
            public void onError(Object object) {
                if (FavouritesFragment.this.isAdded()) {
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
        getActivity().onBackPressed();
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
