package dk.techtify.swipr.dialog.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.IntentHelper;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.model.store.Product;

/**
 * Created by Pavel on 1/9/2017.
 */

public class ShareDialog extends BaseDialog {

    private Product mProduct;

    public void setProduct(Product product) {
        mProduct = product;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_profile_share, null);

//        final ProductPreview productPreview = (ProductPreview) view.findViewById(R.id.preview);
//        productPreview.setProduct(mProduct);

        final String shareText = "http://Share.URL";

        view.findViewById(R.id.whatsapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                IntentHelper.shareTextViaWhatsApp(getActivity(), shareText);
            }
        });

        view.findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                IntentHelper.shareTextViaFacebook(getActivity(), shareText);
            }
        });

        view.findViewById(R.id.mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                IntentHelper.shareTextViaGmail(getActivity(), shareText);
            }
        });

        view.findViewById(R.id.message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                IntentHelper.shareTextViaSms(getActivity(), shareText);
            }
        });

        view.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                IoHelper.copyToClipboard(getActivity(), shareText);
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.success,
                        R.string.link_copied, null);
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