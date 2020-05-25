package com.habeshastudio.fooddelivery.common;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.habeshastudio.fooddelivery.models.User;
import com.habeshastudio.fooddelivery.remote.APIService;
import com.habeshastudio.fooddelivery.remote.GoogleRetrofitClient;
import com.habeshastudio.fooddelivery.remote.IGoogleService;
import com.habeshastudio.fooddelivery.remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Kibrom on 31/03/2019.
 */

public class Common {
    public static final String INTENT_FOOD_ID = "FoodId";
    public static final String DELETE = "Delete";
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    public static String currentKey;
    public static String currentrestaurantID;
    public static String proposedrestaurantID;
    public static Location currentRestaurantLocation;
    private static Location temp = null;
    public static Location currentUserLocation;
    public static boolean alreadyBeenToCart = false;
    public static User currentUser;
    public static String topicName = "News";
    public static String helpUrl = "https://www.googe.com";
    public static String AboutUrl = "https://www.googe.com";
    public static String troubleAuthUrl = "https://www.googe.com";
    public static String PHONE_TEXT = "userPhone";
    public static int totalQuantity = 0;
    public static int totalPrice = 0;
    public static boolean isUsdSelected = false;
    public static double ETB_RATE = 34.0;
    public static HashMap<String, Double> restaurantDistance = new HashMap<>();

    public static APIService getFCMService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapApi() {

        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code) {
        switch (code) {
            case "0":
                return "Your Order is Pending";
            case "1":
                return "Your Food is Ordered";
            case "2":
                return "Food Shipping";
            case "3":
                return "Food Delivered to You";
            case "4":
                return "Cancelled by User";
            case "5":
                return "Cancelled by Admin";
            default:
                return "";
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

    public static Double getDeliveryPrice(){
        if (currentUserLocation!=null){
            double distance = restaurantDistance.get(currentrestaurantID);
            if (distance < 1500) {
                return 20.0;
            } else if (distance < 2000) {
                return 25.0;
            } else if (distance < 3000) {
                return 30.0;
            } else if (distance < 4000) {
                return 35.0;
            } else if (distance < 5000) {
                return 45.0;
            } else if (distance < 6000) {
                return 55.0;
            }else{
                return 100.0;
            }
        }
        else return 0.0;
    }
}
