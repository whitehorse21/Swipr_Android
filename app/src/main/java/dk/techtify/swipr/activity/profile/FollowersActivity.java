package dk.techtify.swipr.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.activity.ChatActivity;
import dk.techtify.swipr.adapter.profile.FollowAdapter;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 12/10/2016.
 */
public class FollowersActivity extends BaseActivity {

    public static final String EXTRA_MODE = "dk.techtify.swipr.activity.profile.FollowersActivity.EXTRA_MODE";
    public static final int MODE_FOLLOWING = 0;
    public static final int MODE_FOLLOWERS = 1;
    public static final int MODE_MESSAGE = 2;

    private int mMode;

    private ActionView mActionView;
    private RecyclerView mRecycler;
    private FollowAdapter mAdapter;
    private EditText mEditable;

    private List<Follow> mFollows;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mMode = getIntent().getExtras().getInt(EXTRA_MODE);

        mActionView = findViewById(R.id.action_view);
        mActionView.setMenuButton(R.drawable.ic_arrow_back);
        mActionView.setTitle(mMode == MODE_MESSAGE ? getString(R.string.new_message) : User.getLocalUser().getName());
        mActionView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mActionView.getActionButton().setVisibility(View.INVISIBLE);
        mActionView.setMenuClickListener(this::onBackPressed);

        mFollows = new ArrayList<>();

        mRecycler = findViewById(R.id.recycler);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.
                VERTICAL, false);
        mRecycler.setLayoutManager(layoutManager);
        mAdapter = new FollowAdapter(this, mMode, mFollows);
        mRecycler.setAdapter(mAdapter);

        if (mMode == MODE_MESSAGE) {
            mAdapter.setRecipientSelectedListener(follower -> {
                Intent intent = new Intent(FollowersActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_RECIPIENT, follower);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        mEditable = findViewById(R.id.editable);
        mEditable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterSuggestions();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (NetworkHelper.isOnline(this, NetworkHelper.ALERT)) {
            getFollows();
        }
    }

    private void filterSuggestions() {
        String keyword = mEditable.getText().toString().trim().toLowerCase();
        if (keyword.length() == 0) {
            mAdapter.addAll(mFollows);
        } else {
            List<Follow> suggestions = new ArrayList<>();
            for (Follow f : mFollows) {
                if (f.getName().toLowerCase().contains(keyword)) {
                    suggestions.add(f);
                }
            }
            mAdapter.addAll(suggestions);
        }
    }

    private void getFollows() {
        mDatabase.child("follow").child(User.getLocalUser().getId()).child(mMode == MODE_FOLLOWING ?
                "following" : "followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    return;
                }

                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                for (Object o : map.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    mFollows.add(new Follow(pair.getKey(), (Map<String, Object>) pair.getValue(),
                            mMode == MODE_FOLLOWING));
                }
                mAdapter.addAll(mFollows);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
