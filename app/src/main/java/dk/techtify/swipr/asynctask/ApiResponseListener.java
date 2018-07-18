package dk.techtify.swipr.asynctask;

public interface ApiResponseListener {

    void onSuccess(Object object);
    void onError(Object object);
}