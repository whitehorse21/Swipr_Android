package dk.techtify.swipr.fragment.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.activity.profile.ActiveBidsActivity;
import dk.techtify.swipr.adapter.profile.ActiveBidsAdapter;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.profile.IncomingBid;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.SwipeLinearLayoutManager;

/**
 * Created by Pavel on 15/11/2016.
 */

public class ActiveBidsFragment extends Fragment implements ActiveBidsAdapter.ActionListener, BaseActivity.IncomeBidsListener {

    private static final String TYPE = "dk.techtify.swipr.fragment.store.ActiveBidsFragment.TYPE";
    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;

    private int mMode;

    private View mProgressBar;
    private RecyclerView mRecycler;
    private ActiveBidsAdapter mAdapter;

    private DatabaseReference mSentBidsReference;

    public static ActiveBidsFragment newInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        ActiveBidsFragment fragment = new ActiveBidsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMode = getArguments().getInt(TYPE);

        View view = inflater.inflate(R.layout.fragment_active_bids, null);

        mProgressBar = view.findViewById(R.id.progress);

        mRecycler = view.findViewById(R.id.recycler);
        final SwipeLinearLayoutManager layoutManager = new SwipeLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(layoutManager);

        List<IncomingBid> list;
        if (mMode == TYPE_SENT) {
            list = new ArrayList<>();
        } else {
            list = ((ActiveBidsActivity) getActivity()).getIncomingBids();
        }
        mAdapter = new ActiveBidsAdapter(getActivity(), mMode, list, layoutManager::setScrollEnabled, this);
        mRecycler.setAdapter(mAdapter);

        if (mMode == TYPE_RECEIVED) {
            ((ActiveBidsActivity) getActivity()).setIncomeBidsListener(this);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mSentBidsReference = FirebaseDatabase.getInstance().getReference("bid-outgoing/" +
                    User.getLocalUser().getId());
            mSentBidsReference.orderByChild("status").equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!ActiveBidsFragment.this.isAdded()) {
                        return;
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        return;
                    }

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    List<IncomingBid> list = new ArrayList<>();
                    for (Object o : map.entrySet()) {
                        Map.Entry pair = (Map.Entry) o;
                        list.add(new IncomingBid(pair.getKey().toString(),
                                (Map<String, Object>) pair.getValue()));
                    }
                    mAdapter.addAll(list);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        ((ActiveBidsActivity) getActivity()).setIncomeBidsListener(null);
        super.onDestroyView();
    }

    @Override
    public void onDeleteClick(IncomingBid bid) {
        if (bid.hasExpired()) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning, mMode ==
                            TYPE_RECEIVED ? R.string.bid_has_expired_decline : R.string.bid_has_expired_cancel,
                    null);
            return;
        }
        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
            if (mMode == TYPE_RECEIVED) {
                ((ActiveBidsActivity) getActivity()).declineIncomingBid(bid);
            } else {
                ((ActiveBidsActivity) getActivity()).cancelOutgoingBid(bid);
                Counters.getInstance().decreaseOutgoingBidsCount();
                mAdapter.removeItem(bid);
            }
        }
    }

    @Override
    public void bidAdded(IncomingBid bid) {
        mAdapter.addItem(bid);
        mRecycler.scrollTo(0, 0);
    }

    @Override
    public void bidRemoved(IncomingBid bid) {
        mAdapter.removeItem(bid);
    }
}