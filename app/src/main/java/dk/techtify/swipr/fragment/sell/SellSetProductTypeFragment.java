package dk.techtify.swipr.fragment.sell;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.adapter.sell.ProductTypeAdapter;
import dk.techtify.swipr.fragment.main.SellFragment;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.FragmentHelper;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.sell.Brand;
import dk.techtify.swipr.model.sell.ProductType;
import dk.techtify.swipr.model.sell.SellProductTypeBrand;

/**
 * Created by Pavel on 15/11/2016.
 */

public class SellSetProductTypeFragment extends Fragment implements ProductTypeAdapter.OnItemClickListener {

    private static final String MODE = "dk.techtify.swipr.fragment.store.SellSetProductTypeFragment.MODE";
    private static final String TOP = "dk.techtify.swipr.fragment.store.SellSetProductTypeFragment.TOP";
    public static final int MODE_PRODUCT_TYPE = 0;
    public static final int MODE_BRAND = 1;

    private int mMode;
    private boolean mIsFirstLaunch = true;

    private List<SellProductTypeBrand> mList;
    private String mLocale;

    private View mEditableLayout, mEditableClear;
    private EditText mEditable;
    private RecyclerView mRecycler;
    private ProductTypeAdapter mAdapter;

    public static SellSetProductTypeFragment newInstance(int mode, int top) {
        Bundle bundle = new Bundle();
        bundle.putInt(MODE, mode);
        bundle.putInt(TOP, top);
        SellSetProductTypeFragment fragment = new SellSetProductTypeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMode = getArguments().getInt(MODE);

        mLocale = Locale.getDefault().getLanguage();

        View view = inflater.inflate(R.layout.fragment_sell_set_product, null);

        mEditableLayout = view.findViewById(R.id.editable_layout);
        mEditableClear = view.findViewById(R.id.clear);
        mEditable = (EditText) view.findViewById(R.id.editable);
        mEditable.setHint(mMode == MODE_PRODUCT_TYPE ? R.string.add_product_type_hint
                : R.string.add_brand_hint);
        mEditable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mEditableClear.setVisibility(i2 > 0 ? View.VISIBLE : View.INVISIBLE);
                filterSuggestions();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEditableClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditable.setText("");
            }
        });
        mEditable.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mEditable.getText().toString().trim()
                        .length() > 0) {
                    if (mMode == MODE_PRODUCT_TYPE) {
                        for (SellProductTypeBrand sptb : mList) {
                            ProductType pt = (ProductType) sptb;
                            if (pt.getDk().toLowerCase().equals(mEditable.getText().toString().trim())
                                    || pt.getEn().toLowerCase().equals(mEditable.getText().toString().trim())) {
                                returnResult(pt);
                                return true;
                            }
                        }

                        ProductType pt = new ProductType(mEditable.getText().toString().trim());
                        returnResult(pt);
                    } else {
                        for (SellProductTypeBrand sptb : mList) {
                            Brand b = (Brand) sptb;
                            if (b.getEn().toLowerCase().equals(mEditable.getText().toString().trim())) {
                                returnResult(b);
                                return true;
                            }
                        }

                        Brand brand = new Brand(mEditable.getText().toString().trim());
                        returnResult(brand);
                    }
                    return true;
                }
                return false;
            }
        });

        mRecycler = (RecyclerView) view.findViewById(R.id.recycler);
        if (Build.VERSION.SDK_INT > 20) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRecycler.getLayoutParams();
            params.topMargin = -DisplayHelper.dpToPx(getActivity(), 24);
            mRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                           int oldTop, int oldRight, int oldBottom) {
                    mRecycler.removeOnLayoutChangeListener(this);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRecycler.getLayoutParams();
                    params.height = params.height + DisplayHelper.dpToPx(getActivity(), 24);
                }
            });
        }

        if (mList == null) {
            mList = new ArrayList<>();
        }

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new ProductTypeAdapter(getActivity(), mList, mLocale, SellSetProductTypeFragment.this);
        mRecycler.setAdapter(mAdapter);

        if (NetworkHelper.isOnline(getActivity(), NetworkHelper.NONE)) {
            getServerProducts();
        }

        mRecycler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SellSetProductTypeFragment.this.isAdded()) {
                    mRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, 350);

        enterAnimation();

        return view;
    }

    private void filterSuggestions() {
        String keyword = mEditable.getText().toString().trim().toLowerCase();
        if (keyword.length() == 0) {
            mAdapter.addAll(mList);
        } else {
            List<SellProductTypeBrand> suggestions = new ArrayList<>();
            if (mMode == MODE_PRODUCT_TYPE) {
                for (SellProductTypeBrand sptb : mList) {
                    ProductType pt = (ProductType) sptb;
                    if (pt.getDk().toLowerCase().contains(keyword) || pt.getEn().toLowerCase()
                            .contains(keyword)) {
                        suggestions.add(pt);
                    }
                }
            } else {
                for (SellProductTypeBrand sptb : mList) {
                    Brand b = (Brand) sptb;
                    if (b.getEn().toLowerCase().contains(keyword)) {
                        suggestions.add(b);
                    }
                }
            }
            mAdapter.addAll(suggestions);
        }
    }

    @Override
    public void onProductClick(SellProductTypeBrand product) {
        IoHelper.hideKeyboard(getActivity(), mEditable);
        returnResult(product);
    }

    private void returnResult(SellProductTypeBrand productType) {
        Fragment fragment = FragmentHelper.getCurrentFragment((AppCompatActivity) getActivity());
        if (fragment instanceof SellFragment) {
            ((SellFragment) fragment).onProductSet(mMode, mLocale, productType);
            getActivity().onBackPressed();
        }
    }

    private void enterAnimation() {
        if (mIsFirstLaunch) {
            mIsFirstLaunch = false;
            mEditableLayout.setTranslationY(DisplayHelper.dpToPx(getActivity(), -8) + getArguments().getInt(TOP));
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mEditableLayout.animate().translationY(0);

                    IoHelper.showKeyboard(getActivity(), mEditable);
                    mEditable.requestFocus();
                }
            });
        } else {
            mEditableLayout.setTranslationY(0);
        }
    }

    @Override
    public void onDestroyView() {
        Fragment fragment = FragmentHelper.getCurrentFragment((MainActivity) getActivity());
        if (fragment instanceof SellFragment) {
            ((SellFragment) fragment).setEditableVisibility(mMode);
        }
        super.onDestroyView();
    }

    private void getServerProducts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mMode == MODE_PRODUCT_TYPE ? "product-type" : "brand").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (SellSetProductTypeFragment.this.isAdded()) {
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        return;
                    }
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Iterator it = map.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();

                        if (mMode == MODE_PRODUCT_TYPE) {
                            mList.add(new ProductType(pair.getKey().toString(), (Map<String, Object>) pair.getValue()));
                        } else {
                            mList.add(new Brand(pair.getKey().toString(), (Map<String, Object>) pair.getValue()));
                        }
                    }
                    mAdapter.addAll(mList);
                    mRecycler.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecycler.scrollTo(0, 0);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (SellSetProductTypeFragment.this.isAdded()) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            databaseError.getMessage() != null ? databaseError.getMessage() :
                                    getString(R.string.error_unknown), null);
                }
            }
        });
    }

    public interface SetProductTypeResult {
        void onProductSet(int mode, String locale, SellProductTypeBrand pt);
    }
}