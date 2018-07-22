package dk.techtify.swipr.fragment.sell;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;

import dk.techtify.swipr.Constants;
import dk.techtify.swipr.R;
import dk.techtify.swipr.activity.MainActivity;
import dk.techtify.swipr.dialog.main.SwiprPlusDialog;
import dk.techtify.swipr.dialog.sell.SellPriceDialog;
import dk.techtify.swipr.dialog.sell.UploadProductDialog;
import dk.techtify.swipr.helper.BitmapHelper;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.IntentHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.sell.Photo;
import dk.techtify.swipr.model.sell.Product;
import dk.techtify.swipr.model.user.Counters;
import dk.techtify.swipr.model.user.User;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 15/11/2016.
 */

public class SellAdditionalInfoFragment extends Fragment implements ActionView.ActionClickListener, SellPriceDialog.PriceListener {

    private Product mProduct;

    public void setProduct(Product mProduct) {
        this.mProduct = mProduct;
    }

    private ScrollView mRootScrollView;
    private View mMoreLayout, mSwiprPlus, mOrLayout;
    private EditText mDescription;
    private TextView mDescriptionCount, mPrice;
    private CheckBox mPayCheckBox;

    public static SellAdditionalInfoFragment newInstance(Product product) {
        if (product.getLocalPhotos() != null) {
            for (Photo p : product.getLocalPhotos()) {
                p.setBitmap(null);
            }
        }
        SellAdditionalInfoFragment fragment = new SellAdditionalInfoFragment();
        fragment.setProduct(product);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setContainerTopMargin(true);
        ((MainActivity) getActivity()).getActionView().setTitle(R.string.sell);
        ((MainActivity) getActivity()).getActionView().setActionButton(R.drawable.ic_close, this);
        ((MainActivity) getActivity()).getActionView().setBackgroundColor(ContextCompat.getColor(
                getActivity(), R.color.colorPrimary));

        final View view = inflater.inflate(R.layout.fragment_sell_additional_info, null);

        mRootScrollView = view.findViewById(R.id.scroll_view);

        mMoreLayout = view.findViewById(R.id.more_visibility_layout);
        if (User.getLocalUser().isPlusMember()) {
            mMoreLayout.setVisibility(View.GONE);
        }

        mDescriptionCount = view.findViewById(R.id.description_counter);
        mDescription = view.findViewById(R.id.description);
        mDescription.setOnTouchListener((v, event) -> {
            (v.getParent()).getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        mDescription.setMovementMethod(new ScrollingMovementMethod());
        mDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(" ")) {
                    mDescription.setText("");
                    return;
                }
                if (s.length() < 24) {
                    mDescriptionCount.setText(String.valueOf(24 - s.length()));
                    mDescriptionCount.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                } else {
                    mDescriptionCount.setText(String.valueOf(1024 - s.length()));
                    mDescriptionCount.setTextColor(ContextCompat.getColor(getActivity(), R.color.textHint));
                }
                mProduct.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (mProduct.getDescription() != null) {
            mDescription.setText(mProduct.getDescription());
        }

        mPrice = view.findViewById(R.id.price);
        mPrice.setOnClickListener(view12 -> {
            SellPriceDialog spd = new SellPriceDialog();
            spd.setPriceListener(SellAdditionalInfoFragment.this);
            spd.setDefaultValue(mProduct.getPrice());
            spd.show(getActivity().getSupportFragmentManager(), spd.getClass().getSimpleName());
        });
        if (mProduct.getPrice() > 0) {
            onPriceSet(mProduct.getPrice());
        }

//        view.findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SellPreviewDialog spd = new SellPreviewDialog();
//                spd.setProduct(mProduct);
//                spd.setActionListener(new SellPreviewDialog.ActionListener() {
//                    @Override
//                    public void onEditClick() {
//                        new Handler().post(new Runnable() {
//                            @Override
//                            public void run() {
//                                getActivity().onBackPressed();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onShareClick(View view) {
//                        savePreviewToBitmap(view);
//                    }
//
//                    @Override
//                    public void onRemoveClick() {
//                        DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
//                                R.string.sell_discard, new DialogHelper.OnActionListener() {
//                                    @Override
//                                    public void onPositive(Object o) {
//                                        new Handler().post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                getActivity().onBackPressed();
//                                                getActivity().onBackPressed();
//                                            }
//                                        });
//                                    }
//                                });
//                    }
//                });
//                spd.show(getActivity().getSupportFragmentManager(), spd.getClass().getSimpleName());
//            }
//        });

        mSwiprPlus = view.findViewById(R.id.swipr_plus);
        mOrLayout = view.findViewById(R.id.or_layout);

        mPayCheckBox = view.findViewById(R.id.pay);
        if (mProduct.getVisibilityMode() == Product.VISIBILITY_MODE_PAID_BOOSTER) {
            oneTimePurchaseSuccessful();
        }
        mPayCheckBox.setOnClickListener(v -> {
            if (mPayCheckBox.isChecked()) {
                mPayCheckBox.setChecked(false);
                ((MainActivity) getActivity()).openPurchaseDialog(Constants.Purchase.ONE_TIME_PRODUCT_BOOSTER);
            }
        });

        mSwiprPlus.setOnClickListener(view1 -> {
            SwiprPlusDialog spd = new SwiprPlusDialog();
            spd.setDealListener((MainActivity) getActivity());
            spd.show(getActivity().getSupportFragmentManager(), spd.getClass().getSimpleName());
        });

        view.findViewById(R.id.next).setOnClickListener(v -> {
            if (!User.getLocalUser().isPlusMember() && Counters.getInstance().getActivePosts() > 4) {
                SwiprPlusDialog spd = new SwiprPlusDialog();
                spd.setDealListener((MainActivity) getActivity());
                spd.show(getActivity().getSupportFragmentManager(), spd.getClass().getSimpleName());
                return;
            }
            if (mProduct.getDescription() == null || mProduct.getDescription().trim().length() < 24) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.add_description, null);
            } else if (mProduct.getPrice() == 0) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.set_price, null);
            } else if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                mProduct.setVisibilityMode(User.getLocalUser().isPlusMember() ? Product
                        .VISIBILITY_MODE_PLUS_BOOSTER : (mPayCheckBox.isChecked() ? Product
                        .VISIBILITY_MODE_PAID_BOOSTER : Product.VISIBILITY_MODE_REGULAR));

                UploadProductDialog upd = new UploadProductDialog();
                upd.setProduct(mProduct);
                upd.setDoneListener(() -> new Handler().post(() -> {
                    getActivity().onBackPressed();
                    getActivity().onBackPressed();

                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.success,
                            R.string.product_added_to_the_store, null);
                }));
                upd.show(getActivity().getSupportFragmentManager(), upd.getClass().getSimpleName());
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActionButtonClick() {
        getActivity().onBackPressed();
    }

    @Override
    public void onPriceSet(int price) {
        mProduct.setPrice(price);
        mPrice.setText(TextUtils.concat(String.valueOf(price), " ", getString(R.string.kr)));
        mPrice.setBackgroundResource(R.drawable.bck_sell_size_inverse);
        mPrice.setTextColor(ContextCompat.getColorStateList(getActivity(),
                R.color.selector_text_white_primary));
    }

    private void savePreviewToBitmap(View view) {
        final Bitmap bitmap = BitmapHelper.getBitmapFromView(view);

        final AlertDialog progress = DialogHelper.getProgressDialog(getContext());
        DialogHelper.showProgressDialog(getContext(), progress);

        new Thread(() -> {
            final File file = BitmapHelper.saveBitmapToCache(getContext(), bitmap);
            bitmap.recycle();

            getActivity().runOnUiThread(() -> {
                if (SellAdditionalInfoFragment.this.isAdded()) {
                    progress.dismiss();

                    IntentHelper.shareFile(getActivity(), file);
                }
            });
        }).start();
    }

    public void oneTimePurchaseSuccessful() {
        mProduct.setVisibilityMode(Product.VISIBILITY_MODE_PAID_BOOSTER);
        mSwiprPlus.setVisibility(View.GONE);
        mOrLayout.setVisibility(View.GONE);
        mPayCheckBox.setChecked(true);
        mPayCheckBox.setEnabled(false);
    }

    public void plusSubscriptionSuccessful() {
        mMoreLayout.setVisibility(View.GONE);
        mProduct.setVisibilityMode(Product.VISIBILITY_MODE_PLUS_BOOSTER);
    }
}