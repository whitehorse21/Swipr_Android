package dk.techtify.swipr.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.makeramen.roundedimageview.RoundedImageView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.DisplayHelper;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/9/2017.
 */

public class ProductPreview extends FrameLayout {

    private int mWidth, mHeight;

    private View mSkeletonView;
    private RoundedImageView mProfileImage;
    private ImageView mProductImage;

    private Product mProduct;

    public void setProduct(Product mProduct) {
        this.mProduct = mProduct;
    }

    public ProductPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        int dp8 = DisplayHelper.dpToPx(context, 8);

        setBackgroundResource(R.drawable.bck_sell_preview_stroke);
        setPadding(dp8, dp8, dp8, dp8);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        LayoutParams skeletonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mSkeletonView = new ImageView(getContext());
        mSkeletonView.setLayoutParams(skeletonParams);
        mSkeletonView.setBackgroundResource(R.drawable.bck_sell_preview_skeleton);
        mSkeletonView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mHeight = bottom - top;
                if (mHeight == 0) {
                    return;
                }
                v.removeOnLayoutChangeListener(this);

                mWidth = mHeight * 598 / 1062;
                mSkeletonView.getLayoutParams().width = mWidth;

                createOtherViews();
            }
        });
        addView(mSkeletonView);
    }

    private void createOtherViews() {
        final User user = User.getLocalUser();
        if (user.getPhotoUrl() != null) {
            LayoutParams profileImageParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            profileImageParams.leftMargin = profileImageParams.rightMargin =
                    mWidth * 207 / 598;
            profileImageParams.topMargin = mHeight * 202 / 1062;
            profileImageParams.bottomMargin = mHeight * (1062 - 202 - 184) / 1062;
            mProfileImage = new RoundedImageView(getContext());
            mProfileImage.setLayoutParams(profileImageParams);
            mProfileImage.setOval(true);
            addView(mProfileImage);
            if (!TextUtils.isEmpty(User.getLocalUser().getPhotoUrl())) {
                GlideApp.with(getContext())
                        .load(FirebaseStorage.getInstance().getReferenceFromUrl(User.getLocalUser().getPhotoUrl()))
                        .into(mProfileImage);
            }
        }

        if (user.getName() != null) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.leftMargin = params.rightMargin = mWidth * 110 / 598;
            params.bottomMargin = mHeight * (1062 - 134) / 1062;
            TextView name = new TextView(getContext());
            name.setLayoutParams(params);
            name.setGravity(Gravity.CENTER);
            name.setTextColor(0xffffffff);
            name.setTextSize(mHeight / 100);
            name.setText(user.getName());
            name.setSingleLine(true);
            addView(name);
        }

        final RatingBar ratingBar = new RatingBar(getContext());
        LayoutParams ratingParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ratingParams.gravity = Gravity.CENTER_HORIZONTAL;
        ratingParams.topMargin = mHeight * 100 / 1062;
        ratingBar.setLayoutParams(ratingParams);
        ratingBar.setScaleX(0.7f);
        ratingBar.setScaleY(0.7f);
        addView(ratingBar);
        ratingBar.post(() -> ratingBar.setRating(user.getRating()));


        if (mProduct != null) {
            if (mProduct.getPhotos() != null && mProduct.getPhotos().size() > 0) {
                LayoutParams productImageParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                productImageParams.topMargin = mHeight * 297 / 1062;
                productImageParams.bottomMargin = mHeight * 398 / 1062;
                mProductImage = new ImageView(getContext());
                mProductImage.setLayoutParams(productImageParams);
                mProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                addView(mProductImage, 0);

                if (!TextUtils.isEmpty(mProduct.getPhotos().get(0))) {
                    GlideApp.with(getContext())
                            .load(FirebaseStorage.getInstance().getReferenceFromUrl(mProduct.getPhotos().get(0)))
                            .into(mProductImage);
                }
//                BitmapHelper.createThumbnailFromPhoto(mProduct.getPhotos()
//                        .get(0), new BitmapHelper.LoadCompleteListener() {
//
//                    @Override
//                    public void onLoadComplete(String path, Bitmap bitmap) {
//                        mProductImage.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    public void onError() {
//
//                    }
//                });
            }

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.topMargin = mHeight * (1062 - 398) / 1062;
            params.bottomMargin = mHeight * 318 / 1062;
            TextView title = new TextView(getContext());
            title.setLayoutParams(params);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(0xffffffff);
            title.setTextSize(mHeight / 100);
            title.setText(mProduct.getBrand().getName().toUpperCase());
            title.setSingleLine(true);
            addView(title);

            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.topMargin = mHeight * (1062 - 318) / 1062;
            params.bottomMargin = mHeight * 230 / 1062;
            TextView price = new TextView(getContext());
            price.setLayoutParams(params);
            price.setGravity(Gravity.CENTER);
            price.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            price.setTextSize(mHeight / 55);
            price.setText(mProduct.getPrice() + " " + getResources().getString(R.string.kr));
            price.setSingleLine(true);
            addView(price);

            if (mProduct.getContactInfo() != null && mProduct.getContactInfo().getCity() != null) {
                params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                params.topMargin = mHeight * (1062 - 226) / 1062;
                params.bottomMargin = mHeight * 174 / 1062;
                TextView city = new TextView(getContext());
                city.setLayoutParams(params);
                city.setGravity(Gravity.CENTER);
                city.setTextColor(ContextCompat.getColor(getContext(), R.color.textSecondary));
                city.setTextSize(mHeight / 130);
                city.setText(mProduct.getContactInfo().getCity());
                city.setSingleLine(true);
                addView(city);
            }

            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = GravityCompat.END;
            params.topMargin = mHeight * (1062 - 490) / 1062;
            params.bottomMargin = mHeight * 398 / 1062;
            TextView size = new TextView(getContext());
            size.setPadding(mWidth / 22, 0, mWidth / 25, 0);
            size.setLayoutParams(params);
            size.setBackgroundResource(R.drawable.bck_sell_preview_size);
            size.setGravity(Gravity.CENTER);
            size.setTextColor(0xffffffff);
            size.setTextSize(mHeight / 85);
            size.setText(String.valueOf(mProduct.getSize()));
            addView(size);
        }
    }
}
