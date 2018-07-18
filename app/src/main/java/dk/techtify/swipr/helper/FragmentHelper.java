package dk.techtify.swipr.helper;

import android.support.annotation.AnimRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import dk.techtify.swipr.R;

/**
 * Created by Pavel on 12/10/2016.
 */

public class FragmentHelper {

    public static Fragment getCurrentFragment(AppCompatActivity activity) {
        return getCurrentFragment(activity, R.id.container);
    }

    public static Fragment getCurrentFragment(AppCompatActivity activity, int id) {
        return activity.getSupportFragmentManager().findFragmentById(id);
    }

    public static void replaceFragmentWithoutAddingToStack(FragmentActivity activity, Fragment fragment) {
        replaceFragment(activity, fragment, 0, 0, 0, 0, false);
    }

    public static void replaceFragment(FragmentActivity activity, Fragment fragment) {
        replaceFragment(activity, fragment, 0, 0, 0, 0, true);
    }

    public static void replaceFragment(FragmentActivity activity, Fragment fragment, @AnimRes int enter,
                                       @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        replaceFragment(activity, fragment, enter, exit, popEnter, popExit, true);
    }

    public static void replaceFragment(FragmentActivity activity, Fragment fragment, @AnimRes int enter,
                                       @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit,
                                       boolean addToStack) {
        if (addToStack) {
            String backStateName = fragment.getClass().getName();

            FragmentManager manager = activity.getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped) {
                FragmentTransaction ft = manager.beginTransaction();
                ft.setCustomAnimations(enter, exit, popEnter, popExit);
                ft.replace(R.id.container, fragment);
                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        } else {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(enter, exit, popEnter, popExit);
            ft.replace(R.id.container, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    public static void replaceChildFragment(Fragment currentFragment, Fragment fragment) {
        replaceChildFragment(currentFragment, fragment, 0, 0, 0, 0);
    }

    public static void replaceChildFragment(Fragment currentFragment, Fragment fragment, @AnimRes int enter,
                                       @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = currentFragment.getChildFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(enter, exit, popEnter, popExit);
            ft.replace(R.id.foreground_container, fragment);
            ft.addToBackStack(backStateName);
            ft.commitAllowingStateLoss();
        }
    }
}
