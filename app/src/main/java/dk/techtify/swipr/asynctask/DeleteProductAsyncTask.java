package dk.techtify.swipr.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;

/**
 * Created by Pavel on 1/26/2017.
 */

public class DeleteProductAsyncTask extends AsyncTask<Void, Void, Object> {

    private String userId;
    private String productId;
    private ApiResponseListener listener;

    public DeleteProductAsyncTask(String userId, String productId, ApiResponseListener listener) {
        this.userId = userId;
        this.productId = productId;
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Void... params) {
        String url = Constants.Url.DELETE_PRODUCT;

        StringBuilder body = new StringBuilder("userId=");
        body.append(userId);
        body.append("&productId=");
        body.append(productId);

        if (AppConfig.DEBUG) {
            Log.d("DELETE PRODUCT", "Request url: " + url);
            Log.d("DELETE PRODUCT", "Request body: " + body.toString());
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
            Log.d("DELETE PRODUCT", "Response: " + response);
        }

        return response.contains("{\"code\":200}");
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