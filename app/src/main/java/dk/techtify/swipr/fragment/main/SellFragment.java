package dk.techtify.swipr.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.activity.sell.AddPhotoActivity;
import dk.techtify.swipr.adapter.sell.SizeAdapter;
import dk.techtify.swipr.dialog.sell.AddContactsDialog;
import dk.techtify.swipr.dialog.sell.AddPhotoDialog;
import dk.techtify.swipr.dialog.sell.SendForReviewDialog;
import dk.techtify.swipr.dialog.sell.TagsDialog;
import dk.techtify.swipr.fragment.sell.SellAdditionalInfoFragment;
import dk.techtify.swipr.fragment.sell.SellSetProductTypeFragment;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.FragmentHelper;
import dk.techtify.swipr.helper.IoHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.sell.Brand;
import dk.techtify.swipr.model.sell.Category;
import dk.techtify.swipr.model.sell.ContactInfo;
import dk.techtify.swipr.model.sell.Photo;
import dk.techtify.swipr.model.sell.Product;
import dk.techtify.swipr.model.sell.ProductType;
import dk.techtify.swipr.model.sell.SellProductTypeBrand;
import dk.techtify.swipr.view.ActionView;
import dk.techtify.swipr.view.SellSizePageTransformer;

/**
 * Created by Pavel on 15/11/2016.
 */

public class SellFragment extends Fragment implements ActionView.ActionClickListener,
        SellSetProductTypeFragment.SetProductTypeResult, AddContactsDialog.ContactsListener,
        AddPhotoDialog.PhotoSelectListener, SendForReviewDialog.SendingResultListener, TagsDialog.TagModifiedListener {

    private ScrollView mScrollView;
    private View mAddProductType, mAddProductTypeLayout, mAddProductTypePlus, mAddBrand,
            mAddBrandLayout, mAddBrandPlus, mForeground;
    private TextView mAddProductTypeResult, mAddBrandResult, mAttachedPhotoCount;
    private ImageButton mPhoto, mContact;

    private View mSizeTitle, mCategoryTitle;
    private ViewPager mSizePager;
    private FlowLayout mCategoryGrid, mTagGrid;
    private SizeAdapter mSizeAdapter;

    public ViewPager getSizePager() {
        return mSizePager;
    }

    private Product mProduct;
    private Map<String, ArrayList<String>> mScales;
    private List<Category> mCategories;

    public Product getProduct() {
        return mProduct;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setContainerTopMargin(true);
        ((MainActivity) getActivity()).getActionView().setTitle(R.string.sell);
        ((MainActivity) getActivity()).getActionView().setActionButton(R.drawable.ic_close, this);
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), R.color.colorPrimary));

        final View view = inflater.inflate(R.layout.fragment_sell, null);

        mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        mForeground = view.findViewById(R.id.foreground);

        mAddProductType = view.findViewById(R.id.add_product_type);
        mAddBrand = view.findViewById(R.id.add_brand);

        mAddProductType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    FragmentHelper.replaceChildFragment(SellFragment.this, SellSetProductTypeFragment
                            .newInstance(SellSetProductTypeFragment.MODE_PRODUCT_TYPE, view.findViewById(
                                    R.id.add_product_type_fl).getTop() - mScrollView.getScrollY()));
                    mAddProductType.setVisibility(View.INVISIBLE);
                    mForeground.animate().alpha(1f);
                }
            }
        });
        mAddBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                    FragmentHelper.replaceChildFragment(SellFragment.this, SellSetProductTypeFragment
                            .newInstance(SellSetProductTypeFragment.MODE_BRAND, view.findViewById(
                                    R.id.add_brand_fl).getTop() - mScrollView.getScrollY()));
                    mAddBrand.setVisibility(View.INVISIBLE);
                    mForeground.animate().alpha(1f);
                }
            }
        });
        mAddProductTypePlus = view.findViewById(R.id.add_product_type_plus);
        mAddProductTypePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddProductType.performClick();
            }
        });
        mAddBrandPlus = view.findViewById(R.id.add_brand_plus);
        mAddBrandPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddBrand.performClick();
            }
        });

        mAddProductTypeLayout = view.findViewById(R.id.add_product_type_result_fl);
        mAddProductTypeResult = (TextView) view.findViewById(R.id.add_product_type_result);
        view.findViewById(R.id.add_product_type_result_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProduct.setProductType(null);
                mProduct.setSize(null);
                mProduct.setCategoryId(null);
                mProduct.setSizePosition(-1);
                mProduct.setSizeScalePosition(-1);

                setAddProductTypeResult(null);

                setCategoryVisibility();
                setSizeVisibility();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (SellFragment.this.isAdded() && mSizePager.getAdapter() != null) {
                            ((SizeAdapter) mSizePager.getAdapter()).clearAll();
                            mSizePager.removeAllViews();
                            mSizePager.setAdapter(null);
                        }
                    }
                }, 200);
            }
        });

        mAddBrandLayout = view.findViewById(R.id.add_brand_result_fl);
        mAddBrandResult = (TextView) view.findViewById(R.id.add_brand_result);
        view.findViewById(R.id.add_brand_result_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProduct.setBrand(null);
                setAddBrandResult(null);
            }
        });

        mSizeTitle = view.findViewById(R.id.size_title);
        mSizePager = (ViewPager) view.findViewById(R.id.size_pager);
        mSizePager.setPageTransformer(false, new SellSizePageTransformer());
        mSizePager.setOffscreenPageLimit(6);
        mSizePager.setPageMargin(-(DisplayHelper.getScreenResolution(getActivity())[0] -
                DisplayHelper.dpToPx(getActivity(), 168)));
        mSizePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    String size = ((SizeAdapter) mSizePager.getAdapter()).getRegisteredFragment(position)
                            .getSizeText();
                    int pos = ((SizeAdapter) mSizePager.getAdapter()).getRegisteredFragment(position)
                            .getSizePosition();
                    if (size != null) {
                        mProduct.setSizePosition(pos);
                        mProduct.setSizeScalePosition(position);
                        mProduct.setSize(size);
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mCategoryTitle = view.findViewById(R.id.category_title);
        mCategoryGrid = (FlowLayout) view.findViewById(R.id.category_grid);

        mTagGrid = (FlowLayout) view.findViewById(R.id.tags_grid);
        mTagGrid.findViewById(R.id.add_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagsDialog td = new TagsDialog();
                if (mProduct.getTags() != null && mProduct.getTags().size() > 0) {
                    td.setDefaultTags(mProduct.getTags());
                }
                td.setTagsAddedListener(SellFragment.this);
                td.show(getActivity().getSupportFragmentManager(), td.getClass().getSimpleName());
            }
        });

        mAttachedPhotoCount = (TextView) view.findViewById(R.id.photo_count);

        mPhoto = (ImageButton) view.findViewById(R.id.add_photo);
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AddPhotoDialog apd = new AddPhotoDialog();
//                if (mProduct.getLocalPhotos() != null && mProduct.getLocalPhotos().size() > 0) {
//                    apd.setPhotos(mProduct.getLocalPhotos());
//                }
//                apd.setPhotoSelectListener(SellFragment.this);
//                apd.show(getActivity().getSupportFragmentManager(), apd.getClass().getSimpleName());
                Intent addPhotoIntent = new Intent(getActivity(), AddPhotoActivity.class);
                if (mProduct.getLocalPhotos() != null) {
                    addPhotoIntent.putExtra(AddPhotoActivity.EXTRA_PHOTO_LIST_INCOME,
                            mProduct.getLocalPhotos());
                }
                getActivity().startActivityForResult(addPhotoIntent, MainActivity.REQUEST_SELL_ADD_PHOTO);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        mContact = (ImageButton) view.findViewById(R.id.add_contacts);
        mContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddContactsDialog acd = new AddContactsDialog();
                acd.setContactsListener(SellFragment.this);
                if (mProduct.getContactInfo() != null) {
                    acd.setContactInfo(mProduct.getContactInfo());
                }
                acd.show(getActivity().getSupportFragmentManager(), acd.getClass().getSimpleName());
            }
        });

        if (mProduct == null) {
            mProduct = new Product();
        } else {
            if (mProduct.getProductType() != null) {
                onProductSet(SellSetProductTypeFragment.MODE_PRODUCT_TYPE, Locale.getDefault()
                        .getLanguage(), mProduct.getProductType());
            }
            if (mProduct.getBrand() != null) {
                onProductSet(SellSetProductTypeFragment.MODE_BRAND, Locale.getDefault()
                        .getLanguage(), mProduct.getBrand());
            }
//            if (mProduct.getSize() != null) {
//                if (mProduct.getProductType() != null && mScales != null) {
//                    showSizeAdapter();
//                }
//            }
            if (mProduct.getLocalPhotos() != null && mProduct.getLocalPhotos().size() > 0) {
                onPhotoSelected(mProduct.getLocalPhotos());
            }
            if (mProduct.getContactInfo() != null) {
                setContactsAddedView();
            }
//            if (mProduct.getCategoryId() != null && mCategories != null) {
//                showCategories();
//            }
        }

        setSizeVisibility();
        setCategoryVisibility();

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFields();
//                goToNextPage();
            }
        });

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            mForeground.setAlpha(1f);
        }

        return view;
    }

    private void setSizeVisibility() {
        mSizeTitle.setVisibility(mProduct.getProductType() == null || mProduct.getProductType().getScaleType() == null ? View.GONE : View.VISIBLE);
        mSizePager.setVisibility(mProduct.getProductType() == null || mProduct.getProductType().getScaleType() == null ? View.GONE : View.VISIBLE);
    }

    private void setCategoryVisibility() {
        mCategoryTitle.setVisibility(mProduct.getProductType() == null ? View.GONE : View.VISIBLE);
        mCategoryGrid.setVisibility(mProduct.getProductType() == null ? View.GONE : View.VISIBLE);
    }

    private void checkFields() {
        if (mProduct.getProductType() == null) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.set_product_type, null);
        } else if (mProduct.getBrand() == null) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.set_brand, null);
        } else if (mProduct.getProductType().getScaleType() != null &&
                (mProduct.getSize() == null || mProduct.getSizeScalePosition() != mSizePager.getCurrentItem())) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.set_size, null);
        } else if (mProduct.getCategoryId() == null) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.select_category, null);
        } else if (mProduct.getLocalPhotos() == null || mProduct.getLocalPhotos().size() == 0) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.add_photo, null);
        } else if (mProduct.getContactInfo() == null) {
            DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                    R.string.add_contacts, null);
        } else if (mProduct.getProductType().getId() == null) {
            SendForReviewDialog sfrd = new SendForReviewDialog();
            sfrd.setMode(SendForReviewDialog.MODE_PRODUCT_TYPE);
            sfrd.setData(mProduct.getProductType());
            sfrd.setSendingResultListener(this);
            sfrd.show(getActivity().getSupportFragmentManager(), sfrd.getClass().getSimpleName());
        } else if (mProduct.getBrand().getId() == null) {
            SendForReviewDialog sfrd = new SendForReviewDialog();
            sfrd.setMode(SendForReviewDialog.MODE_BRAND);
            sfrd.setData(mProduct.getBrand());
            sfrd.setSendingResultListener(this);
            sfrd.show(getActivity().getSupportFragmentManager(), sfrd.getClass().getSimpleName());
        } else {
            goToNextPage();
        }
    }

    @Override
    public void onDestroyView() {
//        ((MainActivity) getActivity()).getActionView().removeActionButton();
        super.onDestroyView();
    }

    @Override
    public void onActionButtonClick() {
        IoHelper.hideKeyboard(getActivity(), mForeground);
        getActivity().onBackPressed();
    }

    public void setEditableVisibility(int mode) {
        if (mode == SellSetProductTypeFragment.MODE_PRODUCT_TYPE) {
            mAddProductType.setVisibility(View.VISIBLE);
        } else {
            mAddBrand.setVisibility(View.VISIBLE);
        }
        mForeground.setAlpha(0f);
    }

    @Override
    public void onProductSet(int mode, String locale, SellProductTypeBrand pt) {
        if (mode == SellSetProductTypeFragment.MODE_PRODUCT_TYPE) {
            if (!NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                return;
            }

            mProduct.setProductType((ProductType) pt);
            setAddProductTypeResult(locale.equals("dk") ? ((ProductType) pt).getDk() :
                    ((ProductType) pt).getEn());

            if (mProduct.getProductType().getScaleType() != null) {
                if (mScales == null) {
                    getScales();
                } else {
                    showSizeAdapter();
                }
            }

//            if (mCategories == null) {
            getCategories();
//            } else {
//                setCategoryVisibility();
//                showCategories();
//            }
        } else {
            mProduct.setBrand((Brand) pt);
            setAddBrandResult(((Brand) pt).getEn());
        }
    }

    private void getCategories() {
        FirebaseDatabase.getInstance().getReference().child("category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (SellFragment.this.isAdded()) {
                    mCategories = new ArrayList<>();

                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>)
                            dataSnapshot.getValue();
                    Iterator it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        mCategories.add(new Category(pair.getKey(), (Map<String, Object>) pair.getValue()));
                    }

                    showCategories();
                    setCategoryVisibility();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showCategories() {
        if (mProduct.getProductType() == null) {
            return;
        }
        mCategoryGrid.removeAllViews();

        FlowLayout.LayoutParams param = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        for (Category c : mCategories) {
            if (mProduct.getProductType().getCategoryIds() != null &&
                    !mProduct.getProductType().getCategoryIds().contains(c.getId())) {
                continue;
            }
            View v = getActivity().getLayoutInflater().inflate(R.layout.item_sell_category, null);
            v.setLayoutParams(param);
            CheckBox cb = (CheckBox) v.findViewById(R.id.title);
            if (mProduct.getCategoryId() != null && mProduct.getCategoryId().equals(c.getId())) {
                cb.setChecked(true);
            } else {
                cb.setChecked(false);
            }
            cb.setText(c.getName());
            cb.setTag(c.getId());
            cb.setOnCheckedChangeListener(mCategoryCheckedListener);
            mCategoryGrid.addView(v);
        }

        if (mCategoryGrid.getChildCount() == 1) {
            ((CheckBox) mCategoryGrid.getChildAt(0).findViewById(R.id.title)).setChecked(true);
        }
    }

    private CompoundButton.OnCheckedChangeListener mCategoryCheckedListener = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mProduct.setCategoryId(buttonView.getTag().toString());
                for (int i = 0; i < mCategoryGrid.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) mCategoryGrid.getChildAt(i).findViewById(R.id.title);
                    if (!buttonView.getTag().equals(cb.getTag()) && cb.isChecked()) {
                        cb.setChecked(false);
                    }
                }
            } else {
                if (mProduct.getCategoryId() != null && mProduct.getCategoryId().equals(
                        buttonView.getTag().toString())) {
                    mProduct.setCategoryId(null);
                }
            }
        }
    };

    private void getScales() {
        FirebaseDatabase.getInstance().getReference().child("scale").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (SellFragment.this.isAdded()) {
                    mScales = (Map<String, ArrayList<String>>) dataSnapshot.getValue();

                    showSizeAdapter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showSizeAdapter() {
        setSizeVisibility();

        mSizeAdapter = new SizeAdapter(getChildFragmentManager(), mScales, mProduct.getProductType()
                .getScaleType());
        mSizePager.setAdapter(mSizeAdapter);
    }

    private void setAddProductTypeResult(String text) {
        mAddProductTypePlus.setVisibility(text == null ? View.VISIBLE : View.INVISIBLE);
        mAddProductType.setClickable(text == null);
        mAddProductTypeLayout.setVisibility(text == null ? View.INVISIBLE : View.VISIBLE);
        mAddProductTypeResult.setText(text == null ? "" : text);
    }

    private void setAddBrandResult(String text) {
        mAddBrandPlus.setVisibility(text == null ? View.VISIBLE : View.INVISIBLE);
        mAddBrand.setClickable(text == null);
        mAddBrandLayout.setVisibility(text == null ? View.INVISIBLE : View.VISIBLE);
        mAddBrandResult.setText(text == null ? "" : text);
    }

    @Override
    public void onContactsAdded(String street, String postCode, String city, String phone, double lat, double lng) {
        mProduct.setContactInfo(new ContactInfo(street, postCode, city, phone, lat, lng));
        setContactsAddedView();
    }

    private void setContactsAddedView() {
        mContact.setBackgroundResource(R.drawable.bck_sell_size_inverse);
        mContact.setImageResource(R.drawable.bck_sell_contact_inverse);
    }

    @Override
    public void onPhotoSelected(ArrayList<Photo> photos) {
        mProduct.setLocalPhotos(photos);
        mAttachedPhotoCount.setText(String.valueOf(photos.size()));
        mPhoto.setBackgroundResource(R.drawable.bck_sell_size_inverse);
        mPhoto.setImageResource(R.drawable.bck_sell_camera_inverse);
    }

    @Override
    public void onSendingForReviewSuccessful(SellProductTypeBrand sellProduct) {
        if (sellProduct instanceof ProductType && mProduct.getBrand().getId() == null) {
            SendForReviewDialog sfrd = new SendForReviewDialog();
            sfrd.setMode(SendForReviewDialog.MODE_BRAND);
            sfrd.setData(mProduct.getBrand());
            sfrd.setSendingResultListener(this);
            sfrd.show(getActivity().getSupportFragmentManager(), sfrd.getClass().getSimpleName());
        } else {
            goToNextPage();
        }
    }

    @Override
    public void onTagAdded(String tag) {
        mProduct.addTag(tag);
        addTagView(tag);
    }

    private void addTagView(String tag) {
        FlowLayout.LayoutParams param = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        View v = getActivity().getLayoutInflater().inflate(R.layout.item_add_tag, null);
        v.setLayoutParams(param);
        TextView tv = (TextView) v.findViewById(R.id.title);
        tv.setText(tag);
        tv.setTag(v);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagGrid.removeView((View) v.getTag());
            }
        });
        mTagGrid.addView(v, mTagGrid.getChildCount() - 1);
    }

    @Override
    public void onTagRemoved(String tag) {
        for (int i = mProduct.getTags().size() - 1; i > -1; i--) {
            if (mProduct.getTags().get(i).equals(tag)) {
                mProduct.getTags().remove(tag);
            }
        }
    }

    private void goToNextPage() {
        FragmentHelper.replaceFragment(getActivity(), SellAdditionalInfoFragment.newInstance(mProduct),
                R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    }
}