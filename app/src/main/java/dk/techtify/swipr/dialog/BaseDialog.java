package dk.techtify.swipr.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import dk.techtify.swipr.R;

/**
 * Created by Pavel on 1/4/2017.
 */

public abstract class BaseDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlidingAnimation;

        dialog.setOnShowListener(d -> {

        });

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            lp.height = SwiprApp.getInstance().getSp().getInt(Constants.Prefs.CONTENT_HEIGHT, 400) -
//                    ((DisplayHelper.hasNavigationBar(getContext()) ?
//                            DisplayHelper.getNavigationBarHeight(getContext(), DisplayHelper
//                                    .getScreenOrientation((Activity) getContext())) : 0));
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(lp);
        }
    }
}
