package com.habeshastudio.fooddelivery.activities.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.Common.Common;
import com.habeshastudio.fooddelivery.R;
import com.rengwuxian.materialedittext.MaterialEditText;

public class Register extends AppCompatActivity {

    public static String name, phone, userStatus;
    MaterialEditText edtPhone, edtName, edtPassword;
    TextView btnTrouble;
    Button btnSignUp;
    RelativeLayout rootLayout;
    DatabaseReference users;
    ProgressDialog mDialog;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        edtPhone = findViewById(R.id.edtPhone);
        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);
        btnTrouble = findViewById(R.id.btn_trouble);
        btnSignUp = findViewById(R.id.btnRegister);
        database = FirebaseDatabase.getInstance();
        rootLayout = findViewById(R.id.root_register_layout);
        users = database.getReference().child("Users");
        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

        isInternet();

        btnTrouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, TroubleAuth.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
        mDialog = new ProgressDialog(Register.this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    mDialog.setMessage("Please wait...");
                    mDialog.show();

                    if (!edtPhone.getText().toString().isEmpty() && !edtPassword.getText().toString().isEmpty() && !edtName.getText().toString().isEmpty()) {

                        name = edtName.getText().toString();
                        phone = edtPhone.getText().toString().trim();

                        users.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(phone).exists()) {
                                    mDialog.dismiss();
                                    Intent phoneAuth = new Intent(Register.this, PhoneAuth.class);
                                    phoneAuth.putExtra("name", name);
                                    phoneAuth.putExtra("phone", phone);
                                    phoneAuth.putExtra("status", dataSnapshot.child(phone).child("status").getValue().toString());
                                    startActivity(phoneAuth);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                    Toast.makeText(Register.this, "Welcome back" + name, Toast.LENGTH_SHORT).show();
                                } else {
                                    mDialog.dismiss();

                                    Intent phoneAuth = new Intent(Register.this, PhoneAuth.class);
                                    phoneAuth.putExtra("name", name);
                                    phoneAuth.putExtra("phone", phone);
                                    phoneAuth.putExtra("status", "new");
                                    startActivity(phoneAuth);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(Register.this, "Please Fill in all info", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                        btnSignUp.startAnimation(animShake);
                    }
                } else {
                    isInternet();
                }
            }
        });


    }

    boolean isInternet() {
        if (!Common.isConnectedToInternet(getBaseContext())) {
            final Snackbar snackbar = Snackbar.make(rootLayout, "Connection lost", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        snackbar.dismiss();
                    }
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            return false;
        }
        return true;
    }
}
