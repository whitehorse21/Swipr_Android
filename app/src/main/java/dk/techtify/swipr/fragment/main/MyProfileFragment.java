package dk.techtify.swipr.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Observable;
import java.util.Observer;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.activity.profile.ActiveBidsActivity;
import dk.techtify.swipr.activity.profile.ActivePostsActivity;
import dk.techtify.swipr.activity.profile.FollowersActivity;
import dk.techtify.swipr.helper.DateTimeHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.RatingBar;

/**
 * Created by Pavel on 15/11/2016.
 */

public class MyProfileFragment extends Fragment implements Observer {

    private DatabaseReference mDatabase;

    private TextView mFollowing, mFollowers, mActivePostsCount, mActiveBidsCount, mSold, mPurchased;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        User user = User.getLocalUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ((MainActivity) getActivity()).setContainerTopMargin(false);
        ((MainActivity) getActivity()).getActionView().setTitle("");
        ((MainActivity) getActivity()).getActionView().removeActionButton();
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), android.R.color.transparent));

        View view = inflater.inflate(R.layout.fragment_my_profile, null);

        view.findViewById(R.id.plus_member).setVisibility(user.isPlusMember() ? View.VISIBLE :
                View.INVISIBLE);

        ((TextView) view.findViewById(R.id.user_name)).setText(user.getName());

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating);
        ((RatingBar) view.findViewById(R.id.rating)).setRating(user.getRating());
        ratingBar.setEnabled(false);

        ((TextView) view.findViewById(R.id.since)).setText(TextUtils.concat(
                getString(R.string.user_since), "\n", DateTimeHelper.getFormattedDate(user.getCreatedAt(),
                        "dd.MM.yyyy")));

        if (user.getContactInfo() != null) {
            ((TextView) view.findViewById(R.id.address)).setText(user.getContactInfo().getCity());
        }

        if (!TextUtils.isEmpty(User.getLocalUser().getPhotoUrl())) {
            GlideApp.with(this)
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(User.getLocalUser().getPhotoUrl()))
                    .into(((ImageView) view.findViewById(R.id.user_photo)));
        }

        mFollowing = (TextView) view.findViewById(R.id.following);
        mFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    Intent intent = new Intent(getActivity(), FollowersActivity.class);
                    intent.putExtra(FollowersActivity.EXTRA_MODE, FollowersActivity.MODE_FOLLOWING);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        mFollowers = (TextView) view.findViewById(R.id.followers);
        mFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    Intent intent = new Intent(getActivity(), FollowersActivity.class);
                    intent.putExtra(FollowersActivity.EXTRA_MODE, FollowersActivity.MODE_FOLLOWERS);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        mActivePostsCount = (TextView) view.findViewById(R.id.active_posts_count);

        mActiveBidsCount = (TextView) view.findViewById(R.id.active_bids_counts);

        mSold = (TextView) view.findViewById(R.id.sold);

        mPurchased = (TextView) view.findViewById(R.id.purchased);

        view.findViewById(R.id.active_posts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    Intent activePostsIntent = new Intent(getActivity(), ActivePostsActivity.class);
                    activePostsIntent.putExtra(ActivePostsActivity.EXTRA_COUNT, mActivePostsCount
                            .getText().toString());
                    getActivity().startActivity(activePostsIntent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        view.findViewById(R.id.active_bids).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    Intent activeBidsIntent = new Intent(getActivity(), ActiveBidsActivity.class);
                    activeBidsIntent.putExtra(ActiveBidsActivity.EXTRA_COUNT, mActiveBidsCount
                            .getText().toString());
                    getActivity().startActivity(activeBidsIntent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        Counters.getInstance().addObserver(this);

        getCounters();

        return view;
    }

    @Override
    public void onDestroyView() {
        Counters.getInstance().deleteObserver(this);

        super.onDestroyView();
    }

    private void getCounters() {
        mFollowing.setText(TextUtils.concat(getString(R.string.following), " ",
                Counters.getInstance().getFollowingString()));
        mFollowers.setText(TextUtils.concat(getString(R.string.followers), " ",
                Counters.getInstance().getFollowersString()));
        mActivePostsCount.setText(Counters.getInstance().getActivePostsString());
        mActiveBidsCount.setText(Counters.getInstance().getActiveBidsString());
        mSold.setText(Counters.getInstance().getSoldString());
        mPurchased.setText(Counters.getInstance().getPurchasedString());
    }

    @Override
    public void update(Observable o, Object arg) {
        getCounters();
    }
}
