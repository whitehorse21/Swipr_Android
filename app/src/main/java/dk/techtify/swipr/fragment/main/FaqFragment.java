package dk.techtify.swipr.fragment.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.adapter.FaqAdapter;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.Faq;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Pavel on 15/11/2016.
 */

public class FaqFragment extends Fragment {

    private RealmResults<Faq> mQuestions;

    private RecyclerView mRecycler;
    private FaqAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setContainerTopMargin(true);
        ((MainActivity) getActivity()).getActionView().setTitle(R.string.faq);
        ((MainActivity) getActivity()).getActionView().removeActionButton();
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), R.color.colorPrimary));

        View view = inflater.inflate(R.layout.fragment_faq, null);

        mQuestions = Realm.getDefaultInstance().where(Faq.class).findAllAsync();
        mQuestions.addChangeListener(new RealmChangeListener<RealmResults<Faq>>() {
            @Override
            public void onChange(RealmResults<Faq> element) {
                if (mRecycler.getAdapter() == null) {
                    mAdapter = new FaqAdapter(getActivity(), element);
                    mRecycler.setAdapter(mAdapter);
                } else {
                    mRecycler.getAdapter().notifyDataSetChanged();
                }
            }
        });

        mRecycler = (RecyclerView) view.findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.NONE)) {
            checkServerFaq();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void checkServerFaq() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("faq").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (FaqFragment.this.isAdded()) {
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        return;
                    }
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Iterator it = map.entrySet().iterator();

                    Realm realm = Realm.getDefaultInstance();
                    if (mQuestions != null && mQuestions.size() > 0) {
                        realm.beginTransaction();
                        mQuestions.deleteAllFromRealm();
                        realm.commitTransaction();
                    }

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();

                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(new Faq(pair.getKey().toString(), (Map<String, Object>) pair.getValue()));
                        realm.commitTransaction();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
