package dk.techtify.swipr.adapter.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/4/2017.
 */

public class FollowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MODE_FOLLOWING = 0;
    public static final int MODE_FOLLOWERS = 1;
    public static final int MODE_MESSAGE = 2;

    private Context mContext;
    private List<Follow> mList;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private int mMode;

    private RecipientSelectedListener mRecipientSelectedListener;

    public void setRecipientSelectedListener(RecipientSelectedListener mRecipientSelectedListener) {
        this.mRecipientSelectedListener = mRecipientSelectedListener;
    }

    public FollowAdapter(Context context, int mode, List<Follow> list) {
        this.mContext = context;
        this.mList = new ArrayList<>();
        this.mList.addAll(list);
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.mStorage = FirebaseStorage.getInstance();
        mMode = mode;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_follow, null);
        RecyclerView.ViewHolder holder = new ProductTypeHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Follow follow = mList.get(position);

        final ProductTypeHolder h = (ProductTypeHolder) holder;

        h.root.setTag(follow);

        h.root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        h.root.setPadding(0, DisplayHelper.dpToPx(mContext, position == 0 ? 12 : 0),
                0, DisplayHelper.dpToPx(mContext, position == mList.size() - 1 ? 12 : 0));

        h.name.setText(mList.get(position).getName());

        if (!TextUtils.isEmpty(follow.getPhotoUrl())) {
            GlideApp.with(mContext)
                    .load(mStorage.getReferenceFromUrl(follow.getPhotoUrl()))
                    .into(h.photo);
        } else {
            h.photo.setImageDrawable(null);
        }

        h.follow.setChecked(follow.isFollowing());
        if (mMode == MODE_FOLLOWERS) {
            mDatabase.child("follow").child(User.getLocalUser().getId()).child("following").child(
                    follow.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    h.follow.setChecked(dataSnapshot != null && dataSnapshot.getValue() != null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (mMode == MODE_MESSAGE) {
            h.follow.setVisibility(View.GONE);
        }

        h.clickable.setClickable(mMode == MODE_MESSAGE);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(List<Follow> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    private class ProductTypeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout root;
        private LinearLayout clickable;
        private ImageView photo;
        private TextView name;
        private CheckBox follow;

        public ProductTypeHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            clickable = itemView.findViewById(R.id.clickable);
            photo = itemView.findViewById(R.id.photo);
            name = itemView.findViewById(R.id.name);
            follow = itemView.findViewById(R.id.follow);
            follow.setOnClickListener(this);
            if (mMode == MODE_MESSAGE) {
                clickable.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            final Follow user = (Follow) root.getTag();

            if (view.getId() == R.id.clickable) {
                mRecipientSelectedListener.onOnRecipientSelected(user);
                return;
            }

            if (!NetworkHelper.isOnline(mContext, NetworkHelper.ALERT)) {
                follow.setChecked(!follow.isChecked());
                return;
            }
            user.setFollowing(follow.isChecked());

            follow.setEnabled(false);

            mDatabase.child("counter").child(user.getId()).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        long counter = dataSnapshot != null && dataSnapshot.getValue() != null ?
                                (Long) dataSnapshot.getValue() : 0;

                        if (follow.isChecked()) {
                            followSeller(user, follow, counter);
                        } else {
                            unfollowSeller(user, follow, counter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    try {
                        follow.setChecked(!follow.isChecked());
                        follow.setEnabled(true);
                        user.setFollowing(follow.isChecked());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void followSeller(final Follow follower, final CheckBox view, long count) {
        final User user = User.getLocalUser();

        Follow they = new Follow(follower.getId(), follower.getName(), follower.getPhotoUrl());
        Follow me = new Follow(user.getId(), user.getName(), user.getPhotoUrl());

        Map<String, Object> myMap = they.toMap();
        Map<String, Object> theirMap = me.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("follow/" + user.getId() + "/following/" + follower.getId(), myMap);
        childUpdates.put("follow/" + follower.getId() + "/followers/" + user.getId(), theirMap);
        childUpdates.put("counter/" + user.getId() + "/following/", Counters.getInstance().getFollowing() + 1);
        childUpdates.put("counter/" + follower.getId() + "/followers/", count + 1);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                view.setChecked(!view.isChecked());
                follower.setFollowing(view.isChecked());
            }

            view.setEnabled(true);
        });
    }

    private void unfollowSeller(final Follow follower, final CheckBox view, long count) {
        User user = User.getLocalUser();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("follow/" + user.getId() + "/following/" + follower.getId(), null);
        childUpdates.put("follow/" + follower.getId() + "/followers/" + user.getId(), null);
        childUpdates.put("counter/" + user.getId() + "/following/", Counters.getInstance().getFollowing() - 1);
        childUpdates.put("counter/" + follower.getId() + "/followers/", count - 1);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                view.setChecked(!view.isChecked());
                follower.setFollowing(view.isChecked());
            }

            view.setEnabled(true);
        });
    }

    public interface RecipientSelectedListener {
        void onOnRecipientSelected(Follow follower);
    }
}