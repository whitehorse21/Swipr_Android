package dk.techtify.swipr.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import dk.techtify.swipr.R;

/**
 * Created by Pavel on 12/14/2016.
 */

public class ActionView extends FrameLayout {

    private MenuClickListener mMenuClickListener;

    private TextView mTitle;
    private ImageButton mMenuButton;
    private ImageButton mActionButton;
    private ImageView mPhoto;

    public ImageButton getActionButton() {
        return mActionButton;
    }

    public ImageView getPhotoView() {
        return mPhoto;
    }

    public void setMenuClickListener(MenuClickListener mMenuClickListener) {
        this.mMenuClickListener = mMenuClickListener;
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        mTitle = findViewById(R.id.title);

        mActionButton = findViewById(R.id.menu_action_view);

        mPhoto = findViewById(R.id.menu_photo);

        mMenuButton = findViewById(R.id.menu);

        mMenuButton.setOnClickListener(view -> {
            if (mMenuClickListener != null) {
                mMenuClickListener.onMenuClick();
            }
        });
    }

    public void setTitle(int title) {
        setTitle(getContext().getString(title));
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setActionButton(@DrawableRes int id, final ActionClickListener clickListener) {
        mActionButton.setVisibility(VISIBLE);
        mActionButton.setImageResource(id);
        mActionButton.setOnClickListener(view -> clickListener.onActionButtonClick());
    }

    public void setMenuButton(@DrawableRes int id) {
        mMenuButton.setImageResource(id);
    }

    public void removeMenuButton() {
        mMenuButton.setVisibility(INVISIBLE);
    }

    public void removeActionButton() {
        mActionButton.setVisibility(INVISIBLE);
        mActionButton.setImageDrawable(null);
        mActionButton.setOnClickListener(null);
    }

    public interface MenuClickListener {

        void onMenuClick();
    }

    public interface ActionClickListener {

        void onActionButtonClick();
    }
}
