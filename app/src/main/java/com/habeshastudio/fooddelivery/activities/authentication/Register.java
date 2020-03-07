package com.habeshastudio.fooddelivery.activities.authentication;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.R;

import java.util.Objects;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Register extends AppCompatActivity {

    public static String name, phone;
    EditText edtPhone, edtName;
    TextView btnTrouble;
    FloatingActionButton btnSignMeUp;
    RelativeLayout rootLayout;
    DatabaseReference users;
    ProgressDialog mDialog;
    AlertDialog dialog_verifying;
    FirebaseDatabase database;
    boolean isNameEditing = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/fr.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_register);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            getWindow().setStatusBarColor(Color.WHITE);
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);




        edtPhone = findViewById(R.id.edtPhone);
        edtName = findViewById(R.id.edtName);
        btnTrouble = findViewById(R.id.btn_trouble);
        btnSignMeUp = findViewById(R.id.btnRegisterMe);
        database = FirebaseDatabase.getInstance();
        rootLayout = findViewById(R.id.root_register_layout);
        users = database.getReference().child("User");
        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

        isInternet();

        btnTrouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, TroubleAuth.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout= inflater.inflate(R.layout.processing_dialog,null);
        AlertDialog.Builder show = new AlertDialog.Builder(Register.this);

        show.setView(alertLayout);
        show.setCancelable(true);
        dialog_verifying = show.create();
        dialog_verifying.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        btnSignMeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {


                    if (!Objects.requireNonNull(edtPhone.getText()).toString().isEmpty() && Integer.parseInt(edtPhone.getText().toString().substring(1))>900000000 ) {

                        dialog_verifying.show();

                        phone = edtPhone.getText().toString().trim();
                        if (phone.startsWith("0"))
                            phone = "+251"  + phone.substring(1);
                        else if (phone.startsWith("9"))
                            phone = "+251"  + phone;
                        else return;
//                        SharedPreferences.Editor editor = getSharedPreferences("userDetail", MODE_PRIVATE).edit();
//                        editor.putString("phone", phone);
//                        editor.putString("name", name);
//                        editor.apply();

                        users.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(phone).exists()) {
                                    name = dataSnapshot.child(phone).child("name").getValue().toString();
                                    dialog_verifying.dismiss();
                                    Intent phoneAuth = new Intent(Register.this, PhoneAuth.class);
                                    phoneAuth.putExtra("name", name);
                                    phoneAuth.putExtra("phone", phone);
                                    phoneAuth.putExtra("status", "not_new");
                                    startActivity(phoneAuth);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                    //Toast.makeText(Register.this, "Welcome back " + name, Toast.LENGTH_SHORT).show();
                                } else {
                                    dialog_verifying.dismiss();
                                    edtName.setVisibility(View.VISIBLE);
                                    if (!Objects.requireNonNull(edtName.getText()).toString().isEmpty() ) {
                                        name = edtName.getText().toString().trim();
                                        Intent phoneAuth = new Intent(Register.this, PhoneAuth.class);
                                        phoneAuth.putExtra("name", name);
                                        phoneAuth.putExtra("phone", phone);
                                        phoneAuth.putExtra("status", "new");
                                        startActivity(phoneAuth);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(Register.this, "Please fill in your name", Toast.LENGTH_SHORT).show();
                                        dialog_verifying.dismiss();
                                        btnSignMeUp.startAnimation(animShake);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    } else {
                        Toast.makeText(Register.this, "Please check your Phone", Toast.LENGTH_SHORT).show();

                        dialog_verifying.dismiss();
                        btnSignMeUp.startAnimation(animShake);
                    }
                } else {
                    btnSignMeUp.startAnimation(animShake);
                    isInternet();
                }
            }
        });
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
}
