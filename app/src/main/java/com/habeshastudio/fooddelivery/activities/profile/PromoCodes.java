package com.habeshastudio.fooddelivery.activities.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.models.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;

public class PromoCodes extends AppCompatActivity {

    DatabaseReference users, database;
    Button submit;
    public ProgressDialog mDialog;
    TextView blockedText, counterText;
    MaterialEditText promocode;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_codes);
        users = FirebaseDatabase.getInstance().getReference("User");
        database = FirebaseDatabase.getInstance().getReference("voucher");
        counterText = findViewById(R.id.counter_note);
        blockedText = findViewById(R.id.blocked_note);
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading ...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        promocode = findViewById(R.id.text_promo_code);
        submit = findViewById(R.id.btn_confirm);

        Paper.init(PromoCodes.this);


        FirebaseDatabase.getInstance().getReference("User").child((Paper.book().read("userPhone").toString()))
                .child("failedVoucherAttempts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    Paper.book().write("counter", Objects.requireNonNull(dataSnapshot.getValue()).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (Paper.book().read("counter") != null)
            if (!Paper.book().read("counter").toString().isEmpty())
                initView(Integer.parseInt(Paper.book().read("counter").toString()));

        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    if (!Objects.requireNonNull(promocode.getText()).toString().isEmpty() && promocode.getText() != null) {
                        final String rPromo = promocode.getText().toString();
                        mDialog.show();
                        database.keepSynced(true);
                        database.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(rPromo).exists()) {
                                    // Balance
                                    final String timeNow = String.valueOf(System.currentTimeMillis());
                                    final double balanceAmount = Double.parseDouble(dataSnapshot.child(rPromo).child("amount").getValue().toString());
                                    final double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) + balanceAmount;
                                    Map<String, Object> update_balance = new HashMap<>();
                                    update_balance.put("balance", balance);
                                    FirebaseDatabase.getInstance().getReference("User")
                                            .child(Paper.book().read("userPhone").toString())
                                            .updateChildren(update_balance)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        database.child(rPromo).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                                hashMap.put("amount", balanceAmount);
                                                                hashMap.put("method", "Recharge using Voucher");
                                                                hashMap.put("comments", rPromo);
                                                                hashMap.put("newBalance", balance);
                                                                FirebaseDatabase.getInstance().getReference("confidential").child("transactionHistory").child("recharge").child(Paper.book().read("userPhone").toString())
                                                                        .child(timeNow).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        database.child(rPromo).removeValue();
                                                                        Paper.book().write("counter", "0");
                                                                        FirebaseDatabase.getInstance().getReference("User").child((Paper.book().read("userPhone").toString()))
                                                                                .child("failedVoucherAttempts").setValue("0");
                                                                        counterText.setVisibility(View.GONE);
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        //Refresh user status
                                                        FirebaseDatabase.getInstance().getReference("User")
                                                                .child(Paper.book().read("userPhone").toString())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                                                        mDialog.dismiss();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(PromoCodes.this, "no such a promo", Toast.LENGTH_SHORT).show();
                                    if (Paper.book().read("counter") != null) {
                                        int counts = Integer.parseInt(Paper.book().read("counter").toString());
                                        Paper.book().write("counter", ++counts);
                                        FirebaseDatabase.getInstance().getReference("User").child((Paper.book().read("userPhone").toString()))
                                                .child("failedVoucherAttempts").setValue(String.valueOf(counts));
                                        initView(counts);
                                    } else {
                                        Paper.book().write("counter", "1");
                                        FirebaseDatabase.getInstance().getReference("User").child((Paper.book().read("userPhone").toString()))
                                                .child("failedVoucherAttempts").setValue("1");
                                        initView(1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        submit.startAnimation(animShake);
                    }
                } else {
                    submit.startAnimation(animShake);
                }
            }
        });
    }

    private void initView(int counts) {
        if (counts == 0) counterText.setVisibility(View.GONE);
        else if (counts < 5) {
            counterText.setText(counts + "/5 " + getResources().getString(R.string.failed_attempts_naccount_will_be_blocked_after_reaching_5));
            counterText.setVisibility(View.VISIBLE);
        } else {
            counterText.setVisibility(View.GONE);
            promocode.setVisibility(View.GONE);
            blockedText.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
