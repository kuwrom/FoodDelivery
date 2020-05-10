package com.habeshastudio.fooddelivery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.habeshastudio.fooddelivery.activities.Home;
import com.habeshastudio.fooddelivery.activities.authentication.Register;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.models.User;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    String phoneNumber, name;
    RelativeLayout rootLayout;
    FirebaseDatabase database;
    private GeoFire geoFire;
    DatabaseReference users, geoRef, geoRestRef;

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
        geoFire = new GeoFire(geoRestRef);
        //geoFire.setLocation("-M6iO4I5aDsX5g6LE8U5", new GeoLocation(13.489735,39.477404));
        //Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();

        //notification
        Paper.init(MainActivity.this);
        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language", "en");
        if (Paper.book().read("usd") != null)
        Common.isUsdSelected  = Paper.book().read("usd");
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
                        }
                        else {
                            final Snackbar snackbar = Snackbar.make(rootLayout, "😫😫Please install play services and try again😫😫", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(Color.YELLOW);
                            snackbar.show();
                        }
                    }
                });

    }
    void doSmtn(){
        if (Common.isConnectedToInternet(getBaseContext())) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {

                //get from firebase
                phoneNumber = user.getPhoneNumber();
                Paper.book().write("userPhone", phoneNumber);
                //currentUser.setPhone(phoneNumber);
                users.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.child(phoneNumber).getValue(User.class);
                        Common.currentUser = currentUser;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, Home.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                }, 2000);
            }
            else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, Register.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                },2000);
            }
        }else isInternet();
    }
    void isInternet() {
        if (!Common.isConnectedToInternet(getBaseContext())) {
            final Snackbar snackbar = Snackbar.make(rootLayout, "Connection lost", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Common.isConnectedToInternet(getBaseContext())) {
                        isInternet();
                    }
                    else
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
    protected void onResume() {
        super.onResume();
        isInternet();
    }
}
