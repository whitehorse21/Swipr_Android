package dk.techtify.swipr.activity.profile;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.Observable;
import java.util.Observer;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.adapter.profile.ActiveBidsPagerAdapter;
import dk.techtify.swipr.helper.GlideApp;
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

        mActionView = findViewById(R.id.action_view);
        mActionView.setMenuButton(R.drawable.ic_arrow_back);
        mActionView.setTitle(User.getLocalUser().getName());
        mActionView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mActionView.getActionButton().setVisibility(View.INVISIBLE);
        mActionView.setMenuClickListener(this::onBackPressed);

        if (!TextUtils.isEmpty(User.getLocalUser().getPhotoUrl())) {
            mActionView.getPhotoView().setVisibility(View.VISIBLE);
            GlideApp.with(this)
                    .load(FirebaseStorage.getInstance().getReferenceFromUrl(User.getLocalUser().getPhotoUrl()))
                    .into(mActionView.getPhotoView());
        }

        mSent = findViewById(R.id.radio_sent);
        mSent.setOnClickListener(v -> setCurrentPage(0));
        mReceived = findViewById(R.id.radio_received);
        mReceived.setOnClickListener(v -> setCurrentPage(1));
        mSentTitle = findViewById(R.id.title_sent);
        mReceivedTitle = findViewById(R.id.title_received);
        mSentCounter = findViewById(R.id.count_sent);
        mReceivedCounter = findViewById(R.id.count_received);

        mCounter = findViewById(R.id.count);

        mPager = findViewById(R.id.pager);
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
