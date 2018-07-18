package dk.techtify.swipr.fragment.store;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;

/**
 * Created by Pavel on 1/23/2017.
 */
public class BidOverviewSliderFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bid_overview_slider, null);

        int sliderWidth = (DisplayHelper.getScreenResolution(getActivity())[0] - DisplayHelper
                .dpToPx(getActivity(), 48)) / 2;

        View slider = view.findViewById(R.id.slider);
        FrameLayout.LayoutParams sliderParams = (FrameLayout.LayoutParams) slider.getLayoutParams();
        sliderParams.width = sliderWidth;

        return view;
    }
}
