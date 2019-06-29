package com.habeshastudio.fooddelivery.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.habeshastudio.fooddelivery.Model.User;
import com.habeshastudio.fooddelivery.Remote.APIService;
import com.habeshastudio.fooddelivery.Remote.GoogleRetrofitClient;
import com.habeshastudio.fooddelivery.Remote.IGoogleService;
import com.habeshastudio.fooddelivery.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by Kibrom on 31/03/2018.
 */

public class Common {

    public static final String INTENT_FOOD_ID = "FoodId";
    public static final String DELETE = "Delete";
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    public static String currentKey;
    public static User currentUser;
    public static String topicName = "News";
    public static String PHONE_TEXT = "userPhone";

    public static APIService getFCMService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapApi() {

        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code) {
        switch (code) {
            case "0":
                return "Placed";
            case "1":
                return "On My Way";
            case "2":
                return "Shipping";
            default:
                return "Shipped";
        }
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat) format).setParseBigDecimal(true);
        return (BigDecimal) format.parse(amount.replace("[^\\d.,]", ""));
    }
}
