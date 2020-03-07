package com.habeshastudio.fooddelivery;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.database.FirebaseDatabase;
import com.habeshastudio.fooddelivery.remote.GpsServices;

public class Derash extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //stopService(new Intent(getBaseContext(), GpsServices.class));
    }
}
