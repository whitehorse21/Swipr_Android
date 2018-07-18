package dk.techtify.swipr.dialog.bid;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.bid.IncomingBidHolderAdapter;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.model.profile.IncomingBid;

/**
 * Created by Pavel on 1/4/2017.
 */

public class IncomingBidHolderDialog extends BaseDialog {

    private IncomingBidHolderAdapter mAdapter;
    private ViewPager mPager;

    public ViewPager getViewPager() {
        return mPager;
    }

    private List<IncomingBid> mIncomingBids;

    public void addIncomingBids(List<IncomingBid> incomingBids) {
        mIncomingBids = new ArrayList<>();
        mIncomingBids.addAll(incomingBids);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_incoming_bid_holder, null);

        mPager = (ViewPager) view.findViewById(R.id.incoming_bid_pager);
        mAdapter = new IncomingBidHolderAdapter(getChildFragmentManager());
        mAdapter.addAllBids(mIncomingBids);
        mPager.setAdapter(mAdapter);

        return view;
    }
}