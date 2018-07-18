package dk.techtify.swipr.dialog.login;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;

/**
 * Created by Pavel on 1/4/2017.
 */

public class TermsConditionsDialog extends BaseDialog {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_terms_conditions, null);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return view;
    }
}