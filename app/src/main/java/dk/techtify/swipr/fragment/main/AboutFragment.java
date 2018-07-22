package dk.techtify.swipr.fragment.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.IntentHelper;

/**
 * Created by Pavel on 15/11/2016.
 */

public class AboutFragment extends Fragment implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerSupportFragment mYouTubePlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setContainerTopMargin(true);
        ((MainActivity) getActivity()).getActionView().setTitle(R.string.about);
        ((MainActivity) getActivity()).getMenuLayer().setVisibility(View.GONE);
        ((MainActivity) getActivity()).getActionView().removeActionButton();
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), R.color.colorPrimary));

        View view = inflater.inflate(R.layout.fragment_about, null);

        if (mYouTubePlayer == null) {
            mYouTubePlayer = new YouTubePlayerSupportFragment();
        }
        getChildFragmentManager().beginTransaction().replace(R.id.video, mYouTubePlayer).commitAllowingStateLoss();
        initializePlayer();

        view.findViewById(R.id.logout).setOnClickListener(view1 -> {
            ((MainActivity) getActivity()).goOffline();
            IntentHelper.logOut(getActivity());
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) getActivity()).getMenuLayer().setVisibility(View.VISIBLE);
        super.onDestroyView();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
                                        boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer.setShowFullscreenButton(false);
            youTubePlayer.cueVideo("1xo3af_6_Jk");
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(getActivity(), MainActivity.REQUEST_PLAYER_RECOVERY).show();
        } else {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning, youTubeInitializationResult.toString(), null);
        }
    }

    public void initializePlayer() {
        mYouTubePlayer.initialize(AppConfig.DEVELOPER_KEY, this);
    }
}
