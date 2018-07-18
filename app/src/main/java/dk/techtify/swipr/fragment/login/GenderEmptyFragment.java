package dk.techtify.swipr.fragment.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * Created by Pavel on 15/11/2016.
 */

public class GenderEmptyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new View(getContext());

        view.setLayoutParams(new AbsListView.LayoutParams(100, 100));

        return view;
    }
}
