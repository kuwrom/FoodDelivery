package com.habeshastudio.fooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;

import java.util.HashMap;
import java.util.Map;

public class Config extends AppCompatActivity {

    FloatingActionButton addName;
    EditText editName, refererphone;
    TextView addReferer;
    DatabaseReference users, referalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        addName = findViewById(R.id.btn_add_name);
        editName = findViewById(R.id.edtName);
        refererphone = findViewById(R.id.edit_referer_phone);
        addReferer = findViewById(R.id.btn_add_referer);

        users = FirebaseDatabase.getInstance().getReference().child("User");
        referalAmount = FirebaseDatabase.getInstance().getReference().child("referralAmount");

        addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                users.child(Common.currentUser.getPhone()).child("name").setValue(editName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Common.currentUser.setName(editName.getText().toString());
                        if (!refererphone.getText().equals("") || refererphone != null) {
                            users.child(refererphone.getText().toString()).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final double balance = Double.parseDouble(dataSnapshot.getValue().toString());
                                    referalAmount.child("newUserReferral").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Map<String, Object> update_balance = new HashMap<>();
                                            update_balance.put("balance", balance + Double.parseDouble(dataSnapshot.getValue().toString()));
                                            users.child(refererphone.getText().toString())
                                                    .updateChildren(update_balance)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            //Toast.makeText(Config.this, "Success", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(Config.this, Home.class));
                                                            finish();
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                        //users.child(refererphone.getText().toString()).child("")
                    }
                });
            }
        });
        addReferer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (refererphone.getVisibility() != View.VISIBLE)
                    refererphone.setVisibility(View.VISIBLE);
                else refererphone.setVisibility(View.GONE);
            }
        });
    }
}