package com.habeshastudio.fooddelivery.Remote;


import com.habeshastudio.fooddelivery.Model.DataMessage;
import com.habeshastudio.fooddelivery.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAlZ6PB6s:APA91bFbIMpmBMFISOfbgSlgpA_LYkfYpjJVZwo_mbEwxkCVZ4PgVofHdzOgUKgmqODsHJe6knLRAHLdczQdGti0z3-BVEwZq2su5bEN9Zn_jFR3_xSikmqgc8so3eZQBE3Us9ZUJQJP"
            }

    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
