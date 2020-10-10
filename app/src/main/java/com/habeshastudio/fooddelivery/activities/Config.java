package com.habeshastudio.fooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;

public class Config extends AppCompatActivity {

    FloatingActionButton addName;
    public static String phoneMe;
    TextView addReferer;
    EditText editName, refererPhone;
    DatabaseReference users, referralAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        Paper.init(this);
        addName = findViewById(R.id.btn_add_name);
        editName = findViewById(R.id.edtName);
        refererPhone = findViewById(R.id.edit_referer_phone);
        addReferer = findViewById(R.id.btn_add_referer);
        users = FirebaseDatabase.getInstance().getReference().child("User");
        referralAmount = FirebaseDatabase.getInstance().getReference().child("referralAmount");

        addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!editName.getText().toString().isEmpty() || editName != null) {
                    User user = new User(Paper.book().read("userPhone").toString());
                    user.setBalance(0.0);
                    user.setPhone(Paper.book().read("userPhone").toString());
                    user.setName(editName.getText().toString());
                    user.setCreatedAt(new SimpleDateFormat
                            ("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
                    Common.currentUser = user;
                    users.child(Paper.book().read("userPhone").toString()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Common.currentUser.setName(editName.getText().toString());
                            if (!Objects.requireNonNull(refererPhone.getText()).toString().isEmpty() && Integer.parseInt(refererPhone.getText().toString().substring(1)) > 900000000) {
                                phoneMe = refererPhone.getText().toString().trim();
                                if (phoneMe.startsWith("0"))
                                    phoneMe = "+251" + phoneMe.substring(1);
                                else if (phoneMe.startsWith("9"))
                                    phoneMe = "+251" + phoneMe;

                                users.child(phoneMe).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            final double balance = Double.parseDouble(Objects.requireNonNull(dataSnapshot.getValue()).toString());
                                            referralAmount.child("newUserReferral").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final Map<String, Object> referralReward = new HashMap<>();
                                                    Map<String, Object> update_balance = new HashMap<>();
                                                    referralReward.put("reward", Double.parseDouble(Objects.requireNonNull(dataSnapshot.getValue()).toString()));
                                                    referralReward.put("referred", Paper.book().read("userPhone").toString());
                                                    update_balance.put("balance", balance + Double.parseDouble(Objects.requireNonNull(dataSnapshot.getValue()).toString()));
                                                    referralReward.put("new Balance", Objects.requireNonNull(update_balance.get("balance")));
                                                    users.child(phoneMe)
                                                            .updateChildren(update_balance)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    String timeNow = String.valueOf(System.currentTimeMillis());
                                                                    FirebaseDatabase.getInstance().getReference("confidential").child("transactionHistory").child("referralReward")
                                                                            .child(phoneMe).child(timeNow).updateChildren(referralReward).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            //Toast.makeText(Config.this, "Success", Toast.LENGTH_SHORT).show();
                                                                            startActivity(new Intent(Config.this, Home.class));
                                                                            finish();
                                                                        }
                                                                    });

                                                                }
                                                            });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        } else
                                            Toast.makeText(Config.this, "User registered without referrer", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            } else {
                                startActivity(new Intent(Config.this, Home.class));
                                finish();
                            }
                        }
                    });
                }
            }
        });
        addReferer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (refererPhone.getVisibility() != View.VISIBLE)
                    refererPhone.setVisibility(View.VISIBLE);
                else refererPhone.setVisibility(View.GONE);
            }
        });
    }
}