package dk.techtify.swipr.activity.profile;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Observable;
import java.util.Observer;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.adapter.profile.ActiveBidsPagerAdapter;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 12/10/2016.
 */
public class ActiveBidsActivity extends BaseActivity implements Observer {

    public static final String EXTRA_COUNT = "dk.techtify.swipr.activity.profile.ActiveBidsActivity.EXTRA_COUNT";

    private ActionView mActionView;
    private TextView mCounter, mSentCounter, mSentTitle, mReceivedCounter, mReceivedTitle;
    private View mSent, mReceived;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_bids);

        mActionView = (ActionView) findViewById(R.id.action_view);
        mActionView.setMenuButton(R.drawable.ic_arrow_back);
        mActionView.setTitle(User.getLocalUser().getName());
        mActionView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mActionView.getActionButton().setVisibility(View.INVISIBLE);
        mActionView.setMenuClickListener(new ActionView.MenuClickListener() {
            @Override
            public void onMenuClick() {
                onBackPressed();
            }
        });

        if (User.getLocalUser().getPhotoUrl() != null) {
            mActionView.getPhotoView().setVisibility(View.VISIBLE);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(User.getLocalUser().getPhotoUrl()))
                    .into(mActionView.getPhotoView());
        }

        mSent = findViewById(R.id.radio_sent);
        mSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentPage(0);
            }
        });
        mReceived = findViewById(R.id.radio_received);
        mReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentPage(1);
            }
        });
        mSentTitle = (TextView) findViewById(R.id.title_sent);
        mReceivedTitle = (TextView) findViewById(R.id.title_received);
        mSentCounter = (TextView) findViewById(R.id.count_sent);
        mReceivedCounter = (TextView) findViewById(R.id.count_received);

        mCounter = (TextView) findViewById(R.id.count);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ActiveBidsPagerAdapter(getSupportFragmentManager()));

        Counters.getInstance().addObserver(this);
        getCounters();
    }

    private void setCurrentPage(int page) {
        mSent.setBackgroundColor(ContextCompat.getColor(this, page == 0 ? R.color.colorPrimary : android.R.color.white));
        mReceived.setBackgroundColor(ContextCompat.getColor(this, page == 1 ? R.color.colorPrimary : android.R.color.white));
        mSentTitle.setTextColor(ContextCompat.getColor(this, page == 0 ? android.R.color.white : R.color.textSecondary));
        mReceivedTitle.setTextColor(ContextCompat.getColor(this, page == 1 ? android.R.color.white : R.color.textSecondary));
        mSentCounter.setTextColor(ContextCompat.getColor(this, page == 0 ? android.R.color.white : R.color.textSecondary));
        mReceivedCounter.setTextColor(ContextCompat.getColor(this, page == 1 ? android.R.color.white : R.color.textSecondary));
        mPager.setCurrentItem(page);
    }

    private void getCounters() {
        mCounter.setText(Counters.getInstance().getActiveBidsString());
        mSentCounter.setText(Counters.getInstance().getActiveBidsSentString());
        mReceivedCounter.setText(Counters.getInstance().getActiveBidsReceivedString());
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void update(Observable o, Object arg) {
        getCounters();
    }

    @Override
    public void onDestroy() {
        Counters.getInstance().deleteObserver(this);
        super.onDestroy();
    }
}
