package dk.techtify.swipr.dialog.sell;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;

import dk.techtify.swipr.R;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.IoHelper;

/**
 * Created by Pavel on 1/4/2017.
 */

public class TagsDialog extends BaseDialog {

    private ArrayList<String> mDefaultTags;

    public void setDefaultTags(ArrayList<String> defaultTags) {
        this.mDefaultTags = defaultTags;
    }

    private FlowLayout mFlow;

    private EditText mEditable;

    private TagModifiedListener mTagModifiedListener;

    public void setTagsAddedListener(TagModifiedListener tagModifiedListener) {
        mTagModifiedListener = tagModifiedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sell_tags, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> getDialog().dismiss());

        view.findViewById(R.id.positive).setOnClickListener(view1 -> getDialog().dismiss());

        mFlow = view.findViewById(R.id.flow);

        mEditable = view.findViewById(R.id.editable);
        mEditable.post(() -> IoHelper.showKeyboard(getActivity(), mEditable));
        mEditable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditable.getText().toString().contains(" ")) {
                    mEditable.setText(mEditable.getText().toString().replaceAll(" ", ""));
                    mEditable.setSelection(mEditable.getText().toString().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditable.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && mEditable.getText().toString().trim()
                    .length() > 0) {

                String newTag = mEditable.getText().toString().trim();
                if (mDefaultTags.contains(newTag)) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.this_tag_already_added, null);
                    return true;
                }
                mDefaultTags.add(newTag);

                mTagModifiedListener.onTagAdded(newTag);

                addTagView(newTag);

                mEditable.setText("");

                return true;
            }
            return false;
        });

        if (mDefaultTags != null) {
            for (String s : mDefaultTags) {
                addTagView(s);
            }
        } else {
            mDefaultTags = new ArrayList<>();
        }

        return view;
    }

    private void addTagView(String tag) {
        FlowLayout.LayoutParams param = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        View v = getActivity().getLayoutInflater().inflate(R.layout.item_add_tag, null);
        v.setLayoutParams(param);
        TextView tv = v.findViewById(R.id.title);
        tv.setText(tag);
        tv.setTag(v);
        tv.setOnClickListener(v1 -> {
            mFlow.removeView((View) v1.getTag());
            mTagModifiedListener.onTagRemoved(((TextView) v1).getText().toString());
        });
        mFlow.addView(v);
    }

    public interface TagModifiedListener {

        void onTagAdded(String tag);

        void onTagRemoved(String tag);
    }
}