package dk.techtify.swipr.dialog.sell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import dk.techtify.swipr.R;
import dk.techtify.swipr.asynctask.ApiResponseListener;
import dk.techtify.swipr.asynctask.GetCoordinatesOfAddressAsyncTask;
import dk.techtify.swipr.dialog.BaseDialog;
import dk.techtify.swipr.helper.DialogHelper;
import dk.techtify.swipr.helper.NetworkHelper;
import dk.techtify.swipr.model.sell.ContactInfo;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/4/2017.
 */

public class AddContactsDialog extends BaseDialog {

    private ContactsListener mContactsListener;
    private ContactInfo mContactInfo;

    public void setContactsListener(ContactsListener contactsListener) {
        mContactsListener = contactsListener;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        mContactInfo = contactInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sell_add_contacts, null);

        view.findViewById(R.id.close).setOnClickListener(view12 -> getDialog().dismiss());

        final EditText street = view.findViewById(R.id.street);
        final EditText postCode = view.findViewById(R.id.post_code);
        final EditText city = view.findViewById(R.id.city);
        final EditText mobile = view.findViewById(R.id.mobile);

        ContactInfo ci = User.getLocalUser().getContactInfo();
        if (ci != null) {
            street.setText(ci.getStreet());
            postCode.setText(ci.getPostCode());
            city.setText(ci.getCity());
            mobile.setText(ci.getMobile());
        }

        if (mContactInfo != null) {
            street.setText(mContactInfo.getStreet());
            postCode.setText(mContactInfo.getPostCode());
            city.setText(mContactInfo.getCity());
            mobile.setText(mContactInfo.getMobile());
        }

        street.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                postCode.requestFocus();
                return true;
            }
            return false;
        });

        postCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                city.requestFocus();
                return true;
            }
            return false;
        });

        view.findViewById(R.id.positive).setOnClickListener(view1 -> {
            if (street.getText().toString().trim().length() < 1
                    || postCode.getText().toString().trim().length() < 1
                    || city.getText().toString().trim().length() < 1
                    || mobile.getText().toString().trim().length() < 1) {
                DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                        R.string.fill_all_fields, null);
                return;
            }
            if (NetworkHelper.isOnline(getActivity(), NetworkHelper.ALERT)) {
                getCoordinates(street.getText().toString().trim(), city.getText().toString().trim(),
                        postCode.getText().toString().trim(),
                        mobile.getText().toString().trim());
            }
        });

        return view;
    }

    private void getCoordinates(final String street, final String city, final String postCode, final String phone) {
        new GetCoordinatesOfAddressAsyncTask(getActivity(), "Denmark, " + city + ", " + street, new ApiResponseListener() {
            @Override
            public void onSuccess(Object object) {
                if (AddContactsDialog.this.isAdded()) {
                    Double[] latLng = (Double[]) object;
                    mContactsListener.onContactsAdded(street, postCode, city, phone, latLng[0], latLng[1]);
                    getDialog().dismiss();
                }
            }

            @Override
            public void onError(Object object) {
                if (AddContactsDialog.this.isAdded()) {
                    DialogHelper.showDialogWithCloseAndDone(getActivity(), R.string.warning,
                            R.string.address_not_found, null);
                }
            }
        }).execute();
    }

    public interface ContactsListener {

        void onContactsAdded(String street, String postCode, String city, String phone, double lat, double lng);
    }
}
