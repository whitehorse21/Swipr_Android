package dk.techtify.swipr.dialog.store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.SwiprApp;
import dk.techtify.swipr.dialog.BaseDialog;

/**
 * Created by Pavel on 1/4/2017.
 */

public class BidSuccessfulDialog extends BaseDialog {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bid_successful, null);

        view.findViewById(R.id.positive).setOnClickListener(view1 -> getDialog().dismiss());

        ((CheckBox) view.findViewById(R.id.don_t_show)).setOnCheckedChangeListener((buttonView, isChecked) -> SwiprApp.getInstance().getSp().edit().putBoolean(Constants.Prefs.SHOW_BID_SUCCESSFUL_ALERT, !isChecked).apply());

        return view;
    }
}