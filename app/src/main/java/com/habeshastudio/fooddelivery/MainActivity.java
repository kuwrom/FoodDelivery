package com.habeshastudio.fooddelivery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.habeshastudio.fooddelivery.activities.Config;
import com.habeshastudio.fooddelivery.activities.Home;
import com.habeshastudio.fooddelivery.activities.authentication.Register;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.models.User;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    LocationManager locationManager;
    String provider;
    String phoneNumber, name;
    RelativeLayout rootLayout;
    FirebaseDatabase database;
    DatabaseReference users, geoRef, geoRestRef, bannerRef;
    private GeoFire geoFire, geofireBanner;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        //startService(new Intent(getBaseContext(), GpsServices.class));

        rootLayout = findViewById(R.id.container_main);
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child("User");

        geoRef = database.getReference("CurrentUserLocation");
        geoRestRef = database.getReference("RerstaurantLocation");
        bannerRef = database.getReference("BannerLocation");
        geoFire = new GeoFire(geoRestRef);
        geofireBanner = new GeoFire(bannerRef);
        //geoFire.setLocation("-MAUzOEGqHd-U2S33MLr", new GeoLocation(13.491389, 39.472424));
//        geofireBanner.setLocation("-M8jPeUC7jhbfvytrjbkjbh", new GeoLocation(13.489697,39.473853));
//        GeoHash geoHash = new GeoHash(new GeoLocation(13.489697,39.473853));
        //Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();

        //notification
        Paper.init(MainActivity.this);


        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language", "en");

        if (Paper.book().read("usd") == null)
            Paper.book().write("usd", false);

        if (Paper.book().read("sub_new") == null) {
            FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
            Paper.book().write("sub_new", "true");
        }

        if (Paper.book().read("usd") != null)
            Common.isUsdSelected = Paper.book().read("usd");
        if (Paper.book().read("restId") != null)
            Common.currentrestaurantID = Paper.book().read("restId");
        if (Paper.book().read("beenToCart") != null)
            Common.alreadyBeenToCart = Paper.book().read("beenToCart");
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            doSmtn();
                        } else {
                            final Snackbar snackbar = Snackbar.make(rootLayout, "😫😫Please install play services and try again 😫😫", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(Color.YELLOW);
                            snackbar.show();
                        }
                    }
                });

    }

    void doSmtn() {
        if (Common.isConnectedToInternet(getBaseContext())) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {

                //get from firebase
                phoneNumber = user.getPhoneNumber();
                Paper.book().write("userPhone", phoneNumber);
                //currentUser.setPhone(phoneNumber);
                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child(phoneNumber).exists()) {
                            startActivity(new Intent(MainActivity.this, Config.class).putExtra("phone", phoneNumber));
                            finish();
                        } else {
                            User currentUser = dataSnapshot.child(phoneNumber).getValue(User.class);
                            Common.currentUser = currentUser;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(MainActivity.this, Home.class));
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                }
                            }, 2000);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, Register.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                }, 2000);
            }
        } else isInternet();
    }

    void isInternet() {
        if (!Common.isConnectedToInternet(getBaseContext())) {
            final Snackbar snackbar = Snackbar.make(rootLayout, getResources().getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Common.isConnectedToInternet(getBaseContext())) {
                        isInternet();
                    } else
                        doSmtn();
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isInternet();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInternet();
    }
}
