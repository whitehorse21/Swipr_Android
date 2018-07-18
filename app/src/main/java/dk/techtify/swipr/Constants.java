package dk.techtify.swipr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pavel on 12/12/2016.
 */

public class Constants {

    public static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    public static final String[] BID_MINS_ARRAY = new String[]{"0", "5", "10", "15", "20", "25", "30",
            "35", "40", "45", "50", "55"};

    public class Prefs {
        public static final String USER = "dk.techtify.swipr.Constants.Prefs.USER";
        public static final String IS_SCREEN_SMALL = "dk.techtify.swipr.Constants.Prefs.IS_SCREEN_SMALL";
        public static final String CONTENT_HEIGHT = "dk.techtify.swipr.Constants.Prefs.CONTENT_HEIGHT";
        public static final String IS_BID_FEE_ALERT_SHOWN = "dk.techtify.swipr.Constants.Prefs.IS_BID_FEE_ALERT_SHOWN";
        public static final String SHOW_BID_SUCCESSFUL_ALERT = "dk.techtify.swipr.Constants.Prefs.SHOW_BID_SUCCESSFUL_ALERT";
        public static final String SERVER_TIME_OFFSET = "dk.techtify.swipr.Constants.Prefs.SERVER_TIME_OFFSET";
        public static final String ONE_SIGNAL_ID = "dk.techtify.swipr.Constants.Prefs.ONE_SIGNAL_ID";
    }

    public class Firebase {
        public static final String BUCKET_NAME = "gs://swipr-dev.appspot.com";
    }

    public class Url {
        private static final String HOST = "https://swipr.techtify.dk/api/api.php";
        public static final String ADD_PRODUCT = HOST + "?add";
        public static final String GET_PRODUCTS = HOST + "?products";
        public static final String GET_FAVOURITES = HOST + "?favorites";
        public static final String DELETE_PRODUCT = HOST + "?remove";
        public static final String PRODUCT_SEEN = HOST + "?shown";
        public static final String ADD_TO_FAVOURITES = HOST + "?add_favorite";
        public static final String DELETE_FROM_FAVOURITES = HOST + "?remove_favorite";
    }

    public static class Purchase {
        public static final String PAYLOAD = "2QClW6tgyJOS7gfY6aXsba70H1t1h1EAQ1ksBE0YADiN7OrTfZhfRVAVse5DbEdZV8de2BJd0a2OdiALrOoTCOIxxw99FBtRJX01vdfgaeT%$G4w";
        public static final String ONE_TIME_PRODUCT_BOOSTER = "one_time_product_booster";
        public static final String SWIPR_PLUS_MEMBERSHIP = "swipr_plus_subscription";
        public static final String[] ID_LIST = new String[]{ONE_TIME_PRODUCT_BOOSTER, SWIPR_PLUS_MEMBERSHIP};
    }
}