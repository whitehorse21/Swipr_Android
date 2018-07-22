package dk.techtify.swipr.dialog.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;

/**
 * Created by Pavel on 1/4/2017.
 */

public class TermsConditionsDialog extends BaseDialog {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_terms_conditions, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> getDialog().dismiss());

        view.findViewById(R.id.positive).setOnClickListener(view1 -> getDialog().dismiss());

        return view;
    }
}