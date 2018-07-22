package dk.techtify.swipr.dialog.sell;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.sell.SellProductTypeBrand;

/**
 * Created by Pavel on 1/4/2017.
 */

public class SendForReviewDialog extends BaseDialog {

    public static final int MODE_PRODUCT_TYPE = 0;
    public static final int MODE_BRAND = 1;

    private int mMode;
    private SellProductTypeBrand mSellProduct;
    private SendingResultListener mSendingListener;

    public void setSendingResultListener(SendingResultListener sendingListener) {
        mSendingListener = sendingListener;
    }

    public void setMode(int modeProductType) {
        mMode = modeProductType;
    }

    public void setData(SellProductTypeBrand sellProduct) {
        mSellProduct = sellProduct;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sell_send_for_review, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> getDialog().dismiss());

        ((TextView) view.findViewById(R.id.title)).setText(mMode == MODE_PRODUCT_TYPE ?
                R.string.send_product_for_review : R.string.send_brand_for_review);

        ((TextView) view.findViewById(R.id.product_name)).setText(mSellProduct.getName());

        view.findViewById(R.id.positive).setOnClickListener(view1 -> {
            if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                sendForReview();
            }
        });

        return view;
    }

    private void sendForReview() {
        final AlertDialog progress = DialogHelper.getProgressDialog(getActivity());
        DialogHelper.showProgressDialog(getActivity(), progress);

        String key = FirebaseDatabase.getInstance().getReference().child("review-product-type")
                .push().getKey();
        mSellProduct.setId(key);
        FirebaseDatabase.getInstance().getReference().child(mMode == MODE_PRODUCT_TYPE ?
                "review-product-type" : "review-brand").child(key).setValue(mSellProduct.getName())
                .addOnCompleteListener(task -> {
                    if (AppConfig.DEBUG) {
                        Log.d("FIREBASE DATABASE", "sendProductForReview:onComplete:" + task.isSuccessful());
                    }

                    progress.dismiss();
                    if (!task.isSuccessful()) {
                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                                task.getException() != null && task.getException()
                                        .getMessage() != null ? task.getException().getMessage() :
                                        getString(R.string.error_unknown), null);
                        return;
                    }

                    mSendingListener.onSendingForReviewSuccessful(mSellProduct);
                    getDialog().dismiss();
                });
    }

    public interface SendingResultListener {
        void onSendingForReviewSuccessful(SellProductTypeBrand sellProduct);
    }
}
