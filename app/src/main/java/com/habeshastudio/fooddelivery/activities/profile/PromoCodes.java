package com.habeshastudio.fooddelivery.activities.profile;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.habeshastudio.fooddelivery.models.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PromoCodes extends AppCompatActivity {

    DatabaseReference users, database;
    Button submit;
    MaterialEditText refererPhone, promocode;
    AlertDialog dialog_verifying;
    private String rPhone, rPromo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_codes);
        users = FirebaseDatabase.getInstance().getReference("User");
        database = FirebaseDatabase.getInstance().getReference("voucher");

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.processing_dialog, null);
        AlertDialog.Builder show = new AlertDialog.Builder(PromoCodes.this);
        show.setView(alertLayout);
        show.setCancelable(true);
        dialog_verifying = show.create();
        dialog_verifying.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        promocode = findViewById(R.id.text_promo_code);
        refererPhone = findViewById(R.id.text_referer_phone);
        submit = findViewById(R.id.btn_confirm);

        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    if (!Objects.requireNonNull(refererPhone.getText()).toString().isEmpty()
                            && !Objects.requireNonNull(promocode.getText()).toString().isEmpty()) {
                        rPhone = refererPhone.getText().toString();
                        rPromo = promocode.getText().toString();
                        dialog_verifying.show();
                        database.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(rPromo).exists()
                                        && dataSnapshot.child(rPromo).child("phone").getValue().toString().equals(rPhone)) {
                                    // Balance
                                    double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) +
                                            Double.parseDouble(dataSnapshot.child(rPromo).child("amount").getValue().toString());
                                    Map<String, Object> update_balance = new HashMap<>();
                                    update_balance.put("balance", balance);
                                    FirebaseDatabase.getInstance().getReference("User")
                                            .child(Common.currentUser.getPhone())
                                            .updateChildren(update_balance)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //Refresh user status
                                                        FirebaseDatabase.getInstance().getReference("User")
                                                                .child(Common.currentUser.getPhone())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                                                        dialog_verifying.dismiss();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                } else {
                                    dialog_verifying.dismiss();
                                    Toast.makeText(PromoCodes.this, "no such a promo", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(PromoCodes.this, "Please Fill in all info", Toast.LENGTH_SHORT).show();
                        submit.startAnimation(animShake);
                    }
                } else {
                    submit.startAnimation(animShake);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
