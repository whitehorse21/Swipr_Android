package dk.techtify.swipr.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import dk.techtify.swipr.AppConfig;
import dk.techtify.swipr.Constants;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/26/2017.
 */

public class AddToFavoritesAsyncTask extends AsyncTask<Void, Void, Integer> {

    private String productId;
    private ApiResponseListener listener;

    public AddToFavoritesAsyncTask(String productId, ApiResponseListener listener) {
        this.productId = productId;
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String url = Constants.Url.ADD_TO_FAVOURITES;

        StringBuilder body = new StringBuilder("userId=");
        body.append(User.getLocalUser().getId());
        body.append("&productId=");
        body.append(productId);
        body.append("&isPlus=");
        body.append(User.getLocalUser().isPlusMember() ? 1 : 0);

        if (AppConfig.DEBUG) {
            Log.d("ADD TO FAVOURITES", "Request url: " + url);
            Log.d("ADD TO FAVOURITES", "Request body: " + body.toString());
        }

        String response;
        try {
            HttpRequest request = HttpRequest.post(url).contentType("application/x-www-form-urlencoded").send(body.toString());
            response = request.body();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        if (response == null) {
            return -1;
        }

        if (AppConfig.DEBUG) {
            Log.d("ADD TO FAVOURITES", "Response: " + response);
        }

        try {
            JSONObject jo = new JSONObject(response);
            int code = jo.getInt("code");
            return code;
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (result == null || result != 200) {
            listener.onError(result);
        } else {
            listener.onSuccess(null);
        }
    }
}