package dk.techtify.swipr.fragment.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.LoginActivity;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.FragmentHelper;
import dk.techtify.swipr.helper.NetworkHelper;

/**
 * Created by Pavel on 12/10/2016.
 */

public class LoginMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(DisplayHelper.isScreenSmall() ? R.layout.fragment_login_main_small : R.layout.fragment_login_main, null);

        view.findViewById(R.id.facebook).setOnClickListener(view14 -> {
            if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                ((LoginActivity) getActivity()).getLoginButton().performClick();
            }
        });

        view.findViewById(R.id.create).setOnClickListener(view13 -> FragmentHelper.replaceFragment(getActivity(), new CreateProfileFragment(),
                R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                R.anim.slide_out_right));

        view.findViewById(R.id.login).setOnClickListener(view12 -> FragmentHelper.replaceFragment(getActivity(), new LoginFragment(),
                R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                R.anim.slide_out_right));

        view.findViewById(R.id.anonymous_login).setOnClickListener(view1 -> {
            if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                ((LoginActivity) getActivity()).anonymousLogin();
            }
        });

        return view;
    }
}
