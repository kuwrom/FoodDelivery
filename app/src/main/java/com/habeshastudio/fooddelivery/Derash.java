package com.habeshastudio.fooddelivery;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;

public class Derash extends Application {
    public static Derash instance;

    public static Derash getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //stopService(new Intent(getBaseContext(), GpsServices.class));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAtach(base, "en"));
    }
}
