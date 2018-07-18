package dk.techtify.swipr.fragment.login;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cocosw.bottomsheet.BottomSheet;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.LoginActivity;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.helper.PermissionHelper;
import dk.techtify.swipr.view.GenderSelectorView;

/**
 * Created by Pavel on 12/10/2016.
 */

public class LoginFragment extends Fragment {

    private EditText mEmail, mPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(DisplayHelper.isScreenSmall() ? R.layout.small_fragment_log_in : R.layout.fragment_log_in, null);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mEmail = (EditText) view.findViewById(R.id.email);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassword.setTransformationMethod(new PasswordTransformationMethod());

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!IoHelper.isEmailValid(mEmail.getText().toString().trim())) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.enter_email, null);
                } else if (mPassword.getText().toString().length() < 6) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.enter_password, null);
                } else if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    IoHelper.hideKeyboard(getActivity(), mPassword);
                    ((LoginActivity) getActivity()).loginWithEmailAndPassword(
                            mEmail.getText().toString().trim(),
                            mPassword.getText().toString().trim());
                } else {
                    IoHelper.hideKeyboard(getActivity(), mPassword);
                }
            }
        });

        return view;
    }
}