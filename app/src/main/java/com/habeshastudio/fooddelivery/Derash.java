package com.habeshastudio.fooddelivery;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

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

        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
        picassoBuilder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = picassoBuilder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
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
