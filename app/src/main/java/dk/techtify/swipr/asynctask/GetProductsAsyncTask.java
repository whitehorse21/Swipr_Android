package dk.techtify.swipr.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.model.store.Product;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/26/2017.
 */

public class GetProductsAsyncTask extends AsyncTask<Void, Void, Object> {

    private static final int PART_SIZE = 10;

    private String lastId;
    private ApiResponseListener listener;

    public GetProductsAsyncTask(String lastId, ApiResponseListener listener) {
        this.lastId = lastId;
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Void... params) {
        StringBuilder url = new StringBuilder(Constants.Url.GET_PRODUCTS);
        url.append("&userId=");
        url.append(User.getLocalUser().getId());
        url.append("&limit=");
        url.append(PART_SIZE);
        url.append("&isPlus=");
        url.append(User.getLocalUser().isPlusMember() ? 1 : 0);
        if (lastId != null) {
            url.append("&lastId=");
            url.append(lastId);
        }

        if (AppConfig.DEBUG) {
            Log.d("REQUEST URL", url.toString());
        }

        String response;
        try {
            HttpRequest request = HttpRequest.get(url.toString());
            response = request.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (response == null) {
            return null;
        }

        if (AppConfig.DEBUG) {
            Log.d("RESPONSE BODY", response);
        }

        try {
            JSONArray ja = new JSONArray(response);
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) {
                products.add(new Product(ja.getJSONObject(i)));
            }

            return products;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);

        if (result == null) {
            listener.onError(null);
        } else {
            listener.onSuccess(result);
        }
    }
}