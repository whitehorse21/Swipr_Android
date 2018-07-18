package dk.techtify.swipr.dialog.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.view.PlusHandView;

/**
 * Created by Pavel on 1/4/2017.
 */

public class SwiprPlusDialog extends BaseDialog {

    private DealListener mDealListener;

    public void setDealListener(DealListener dealListener) {
        this.mDealListener = dealListener;
    }

    private PlusHandView mPlusHandView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
//                DisplayHelper.pxToDp(getActivity(), DisplayHelper.getScreenResolution(getActivity())
//                        [1]) < 561 ? R.layout.dialog_swipr_plus_small : R.layout.dialog_swipr_plus,
                R.layout.dialog_swipr_plus_scroll, null);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        mPlusHandView = (PlusHandView) view.findViewById(R.id.hand);
        mPlusHandView.setAnimationDoneListener(new PlusHandView.AnimationDoneListener() {
            @Override
            public void onAnimationDone() {
                if (mDealListener != null) {
                    mDealListener.swiprPlusDeal();
                }
                getDialog().dismiss();
            }
        });

        view.findViewById(R.id.sizer).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                       int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);

                mPlusHandView.setHandParams(bottom - top);
            }
        });

        return view;
    }

    public interface DealListener {
        void swiprPlusDeal();
    }
}