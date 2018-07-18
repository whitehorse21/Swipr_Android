package dk.techtify.swipr.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;

/**
 * Created by Pavel on 4/11/2016.
 */
public class DialogHelper {

    public static AlertDialog getProgressDialog(Context context) {
        View custom = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setCustomTitle(custom);

        AlertDialog d = dialog.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return d;
    }

    public static void showDialogWithCloseAndDone(Context context, int title, int text, OnActionListener listener) {
        showDialogWithCloseAndDone(context, context.getResources().getString(title),
                context.getResources().getString(text), false, listener);
    }

    public static void showDialogWithCloseAndDone(Context context, String title, String text, OnActionListener listener) {
        showDialogWithCloseAndDone(context, title, text, false, listener);
    }

    public static void showDialogWithCloseAndDone(Context context, int title, String text, OnActionListener listener) {
        showDialogWithCloseAndDone(context, context.getResources().getString(title), text, false, listener);
    }

    public static void showDialogWithCloseAndDone(Context context, String title, String text,
                                                  boolean gravityStart, final OnActionListener listener) {
        View custom = LayoutInflater.from(context).inflate(R.layout.dialog_close_done, null);
        if (gravityStart) {
            ((TextView) custom.findViewById(R.id.content)).setGravity(GravityCompat.START);
        }
        ((TextView) custom.findViewById(R.id.title)).setText(title);
        ((TextView) custom.findViewById(R.id.content)).setText(text);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCustomTitle(custom);
        dialog.setCancelable(true);

        final AlertDialog d = dialog.create();
//        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = DisplayHelper.dpToPx(context, 320);
        d.getWindow().setAttributes(lp);

        custom.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        custom.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                if (listener != null) {
                    listener.onPositive(null);
                }
            }
        });
    }

    public static void showProgressDialog(Context context, AlertDialog d) {
        d.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = DisplayHelper.dpToPx(context, 132);
        d.getWindow().setAttributes(lp);
    }

    public static class OnActionListener {
        public void onPositive(Object o) {
        }

        public void onNegative(Object o) {
        }

        public void onDismiss() {
        }
    }

    public static void showAddPhotoDialog(Context context, final OnActionListener listener) {
        View custom = LayoutInflater.from(context).inflate(R.layout.dialog_add_photo_picker, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCustomTitle(custom);
        dialog.setCancelable(true);

        final AlertDialog d = dialog.create();
//        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = DisplayHelper.dpToPx(context, 320);
        d.getWindow().setAttributes(lp);

        custom.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        custom.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                if (listener != null) {
                    listener.onPositive(0);
                }
            }
        });

        custom.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                if (listener != null) {
                    listener.onPositive(1);
                }
            }
        });
    }
}
