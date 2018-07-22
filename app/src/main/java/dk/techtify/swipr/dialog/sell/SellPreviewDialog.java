package dk.techtify.swipr.dialog.sell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.model.sell.Product;
import dk.techtify.swipr.view.ProductPreview;

/**
 * Created by Pavel on 1/9/2017.
 */

@Deprecated
public class SellPreviewDialog extends BaseDialog {

    private Product mProduct;

    public void setProduct(Product product) {
        mProduct = product;
    }

    private ActionListener mActionListener;

    public void setActionListener(ActionListener mActionListener) {
        this.mActionListener = mActionListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sell_preview, null);

        final ProductPreview productPreview = view.findViewById(R.id.preview);
//        productPreview.setProduct(mProduct);

        view.findViewById(R.id.edit).setOnClickListener(v -> {
            getDialog().dismiss();
            mActionListener.onEditClick();
        });

        view.findViewById(R.id.share).setOnClickListener(v -> {
            getDialog().dismiss();
            mActionListener.onShareClick(productPreview);
        });

        view.findViewById(R.id.remove).setOnClickListener(v -> {
            getDialog().dismiss();
            mActionListener.onRemoveClick();
        });

        view.findViewById(R.id.positive).setOnClickListener(view1 -> getDialog().dismiss());

        return view;
    }

    public interface ActionListener {
        void onEditClick();

        void onShareClick(View view);

        void onRemoveClick();
    }
}