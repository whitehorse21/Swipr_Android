package dk.techtify.swipr.asynctask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import dk.techtify.swipr.AppConfig;

/**
 * Created by Pavel on 1/31/2017.
 */

public class GetCoordinatesOfAddressAsyncTask extends AsyncTask<Void, Void, Double[]> {

    private Context context;
    private String address;
    private ApiResponseListener listener;

    public GetCoordinatesOfAddressAsyncTask(Context context, String address, ApiResponseListener listener) {
        this.context = context;
        this.address = address;
        this.listener = listener;
    }

    @Override
    protected Double[] doInBackground(Void... params) {
        StringBuilder url = new StringBuilder("http://maps.google.com/maps/api/geocode/json?address=");
        try {
            url.append(URLEncoder.encode(address, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (AppConfig.DEBUG) {
            Log.d("GEOCODER", "Request: " + url.toString());
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
            Log.d("GEOCODER", "Response: " + response);
        }

        try {
            JSONObject jo = new JSONObject(response);
            JSONArray ja = jo.getJSONArray("results");
            jo = ja.getJSONObject(0);
            jo = jo.getJSONObject("geometry");
            jo = jo.getJSONObject("location");

            return new Double[]{jo.getDouble("lat"), jo.getDouble("lng")};
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Double[] result) {
        super.onPostExecute(result);

        if (result == null) {
            listener.onError(null);
        } else {
            listener.onSuccess(result);
        }
    }
}