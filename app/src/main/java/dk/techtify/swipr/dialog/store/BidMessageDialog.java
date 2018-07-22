package dk.techtify.swipr.dialog.store;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.model.store.OutgoingBid;

/**
 * Created by Pavel on 1/4/2017.
 */

public class BidMessageDialog extends BaseDialog {

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
            mMessageListener.onBack();
        }
        super.onDismiss(dialog);
    }

    private MessageListener mMessageListener;

    public void setMessageListener(MessageListener messageListener) {
        mMessageListener = messageListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mBid == null) {
            mBid = new OutgoingBid();
        }
        View view = inflater.inflate(R.layout.dialog_bid_message, null);

        view.findViewById(R.id.close).setOnClickListener(view13 -> {
            mCancelBid = true;
            getDialog().dismiss();
        });

        view.findViewById(R.id.back).setOnClickListener(view12 -> getDialog().dismiss());

        final EditText message = view.findViewById(R.id.message);
        if (mBid.getMessage() != null) {
            message.setText(mBid.getMessage());
            message.setSelection(message.getText().toString().length());
        }
        message.post(() -> IoHelper.showKeyboard(getActivity(), message));

        view.findViewById(R.id.positive).setOnClickListener(view1 -> {
            mMessageListener.onMessageAdded(message.getText().toString().trim());
            getDialog().dismiss();
        });

        return view;
    }

    public interface MessageListener {

        void onMessageAdded(String message);

        void onBack();
    }
}