package dk.techtify.swipr.activity.profile;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.BaseActivity;
import dk.techtify.swipr.adapter.profile.ActivePostsAdapter;
import dk.techtify.swipr.asynctask.ApiResponseListener;
import dk.techtify.swipr.dialog.profile.ShareDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.FirebaseHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.view.ActionView;
import dk.techtify.swipr.view.SwipeLinearLayoutManager;

/**
 * Created by Pavel on 12/10/2016.
 */
public class ActivePostsActivity extends BaseActivity implements ActivePostsAdapter.ActionListener {

    public static final String EXTRA_COUNT = "dk.techtify.swipr.activity.profile.ActivePostsActivity.EXTRA_COUNT";

    private ActionView mActionView;
    private TextView mCounter;
    private RecyclerView mRecycler;
    private ActivePostsAdapter mAdapter;

    private List<Product> mProducts;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_posts);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        mProducts = new ArrayList<>();

        mRecycler = (RecyclerView) findViewById(R.id.recycler);
        final SwipeLinearLayoutManager layoutManager = new SwipeLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(layoutManager);
        mAdapter = new ActivePostsAdapter(this, mProducts, new ActivePostsAdapter.ParentSwipeListener() {
            @Override
            public void onParentSwipeEnable(boolean enable) {
                layoutManager.setScrollEnabled(enable);
            }
        }, this);
        mRecycler.setAdapter(mAdapter);

        mCounter = (TextView) findViewById(R.id.count);
        mCounter.setText(getIntent().getStringExtra(EXTRA_COUNT));

        getServerProducts();
    }

    private void getServerProducts() {
        FirebaseDatabase.getInstance().getReference().child("user-product").child(User.getLocalUser()
                .getId()).orderByChild("status").equalTo(0).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    return;
                }

                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                ((TextView) findViewById(R.id.count)).setText(String.valueOf(map.size()));
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    mProducts.add(new Product(pair.getKey().toString(), (Map<String, Object>) pair.getValue()));
                }

                mAdapter.notifyDataSetChanged();
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

    @Override
    public void onShare(int position) {
        ShareDialog sd = new ShareDialog();
        sd.setProduct(mProducts.get(position));
        sd.show(getSupportFragmentManager(), sd.getClass().getSimpleName());
    }

    @Override
    public void onDelete(final int position) {
        if (NetworkHelper.isOnline(this, NetworkHelper.ALERT)) {
            DialogHelper.showDialogWithCloseAndDone(this, R.string.warning, R.string.delete_product,
                    new DialogHelper.OnActionListener() {
                        @Override
                        public void onPositive(Object o) {
                            if (NetworkHelper.isOnline(ActivePostsActivity.this, NetworkHelper.ALERT)) {
                                Product product = mProducts.get(position);
                                FirebaseHelper.deleteMyProduct(product.getUserId(), product.getId(), new ApiResponseListener() {
                                    @Override
                                    public void onSuccess(Object object) {

                                    }

                                    @Override
                                    public void onError(Object object) {
                                        DialogHelper.showDialogWithCloseAndDone(ActivePostsActivity
                                                .this, R.string.warning, R.string.error_unknown, null);
                                    }
                                });

                                mAdapter.removeItem(position);
                            }
                        }
                    });
        }
    }
}
