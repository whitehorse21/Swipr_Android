package dk.techtify.swipr.dialog.store;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.store.BidOverviewSliderAdapter;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.store.OutgoingBid;

/**
 * Created by Pavel on 1/4/2017.
 */

public class BidOverviewDialog extends BaseDialog {

    private boolean mCancelBid = false;

    private OutgoingBid mBid;

    public void setBid(OutgoingBid bid) {
        this.mBid = bid == null ? new OutgoingBid() : bid;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlidingAnimationReverse;

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!mCancelBid) {
            mDoneListener.onBack();
        }
        super.onDismiss(dialog);
    }

    private DoneListener mDoneListener;

    public void setDoneListener(DoneListener doneListener) {
        mDoneListener = doneListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mBid == null) {
            mBid = new OutgoingBid();
        }
        View view = inflater.inflate(R.layout.dialog_bid_overview, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> {
            mCancelBid = true;
            getDialog().dismiss();
        });

        view.findViewById(R.id.back).setOnClickListener(view1 -> getDialog().dismiss());

        ((TextView) view.findViewById(R.id.price)).setText(TextUtils.concat(String.valueOf(mBid
                .getPrice()), " ", getString(R.string.kr)));

        if (mBid.getTimerMinPosition() != 0 || mBid.getTimerHrsPosition() != 0) {
            ((TextView) view.findViewById(R.id.timer)).setText(TextUtils.concat(String.valueOf(
                    mBid.getTimerHrsPosition()), " ", mBid.getTimerHrsPosition() == 1 ? getString(
                    R.string.hr) : getString(R.string.hrs), " ", String.valueOf(mBid
                    .getTimerMinPosition() * 5), " ", getString(R.string.min)));
        } else {
            view.findViewById(R.id.timer).setVisibility(View.GONE);
        }

        if (!mBid.isIncludeShipping()) {
            view.findViewById(R.id.shipping).setVisibility(View.GONE);
        }

        if (mBid.getMessage() != null) {
            ((TextView) view.findViewById(R.id.message)).setText(mBid.getMessage());
        } else {
            view.findViewById(R.id.message).setVisibility(View.GONE);
        }

        int sliderWidth = (DisplayHelper.getScreenResolution(getActivity())[0] - DisplayHelper
                .dpToPx(getActivity(), 48)) / 2;

        final ViewPager slidePager = view.findViewById(R.id.pager);
        slidePager.setPageMargin(-sliderWidth);
        slidePager.setAdapter(new BidOverviewSliderAdapter(getChildFragmentManager()));
        slidePager.setCurrentItem(1, false);
        slidePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (mBid.getPrice() == 0) {
                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                R.string.set_price, null);
                        slidePager.setCurrentItem(1, true);
                    }
                    if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                        mCancelBid = true;
                        getDialog().dismiss();
                        mDoneListener.onDone();
                    } else {
                        slidePager.setCurrentItem(1, true);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    public interface DoneListener {

        void onDone();

        void onBack();
    }
}