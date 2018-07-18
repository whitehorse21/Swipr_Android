package dk.techtify.swipr.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;

/**
 * Created by Pavel on 1/26/2017.
 */

public class ProductSeenAsyncTask extends AsyncTask<Void, Void, Object> {

    private String userId;
    private String productId;

    public ProductSeenAsyncTask(String userId, String productId) {
        this.userId = userId;
        this.productId = productId;
    }

    @Override
    protected Object doInBackground(Void... params) {
        String url = Constants.Url.PRODUCT_SEEN;

        StringBuilder body = new StringBuilder("userId=");
        body.append(userId);
        body.append("&productId=");
        body.append(productId);

        if (AppConfig.DEBUG) {
            Log.d("PRODUCT SEEN", "Request url: " + url);
            Log.d("PRODUCT SEEN", "Request body: " + body.toString());
        }

        String response;
        try {
            HttpRequest request = HttpRequest.post(url).contentType("application/x-www-form-urlencoded").send(body.toString());
            response = request.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (response == null) {
            return null;
        }

        if (AppConfig.DEBUG) {
            Log.d("PRODUCT SEEN", "Response: " + response);
        }

        return null;
    }
}