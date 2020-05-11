package com.habeshastudio.fooddelivery.activities.authentication;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.MainActivity;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.activities.Home;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class PhoneAuth extends AppCompatActivity {

    public String username, phone, status;
    TextView textTitle, firstText, phoneText, secondText, btnTrouble;
    Button btnContinue;
    PinView pinView;
    boolean firstTime = true;
    int interval = 1;
    ProgressBar progressBar;
    boolean canResend = false;
    boolean canVerify = false;
    DatabaseReference users;
    ProgressDialog mDialog;
    FirebaseDatabase database;
    boolean isJobDone = false;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;

    @Override
    protected void attachBaseContext(Context newBase) {
        //super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/fr.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_phone_auth);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        Intent intent = getIntent();
        username = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        status = intent.getStringExtra("status");

        //view
        progressBar = findViewById(R.id.progressBarResend);
        textTitle = findViewById(R.id.auth_title_text);
        firstText = findViewById(R.id.auth_first_text);
        phoneText = findViewById(R.id.auth_Phone_label);
        secondText = findViewById(R.id.auth_second_text);
        btnContinue = findViewById(R.id.btnVerify);
        btnTrouble = findViewById(R.id.btn_trouble);
        pinView = findViewById(R.id.pinView);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child("User");
        mDialog = new ProgressDialog(PhoneAuth.this);

        phoneText.setText(new StringBuilder(phone));
        textTitle.setText(new StringBuilder(getResources().getString(R.string.dear) + " " + username));

        btnTrouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PhoneAuth.this, TroubleAuth.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        secondText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlString = "https://dine.flycricket.io/privacy.html";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.chrome");
                try {
                    PhoneAuth.this.startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    intent.setPackage(null);
                    PhoneAuth.this.startActivity(intent);
                }
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstTime) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        btnContinue.setText(getString(R.string.verify));
                        canVerify = true;
                        phoneText.setVisibility(View.GONE);
                        pinView.setVisibility(View.VISIBLE);
                        textTitle.setText(new StringBuilder(getString(R.string.code_sent_to)+ " " + phone));
                        secondText.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        firstTime = false;
                        new CountDownTimer(60000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (isJobDone)
                                    cancel();
                                firstText.setText(new StringBuilder(getString(R.string.arrival_count_down) + " " +(millisUntilFinished / 1000)) );
                                progressBar.setProgress(progressBar.getProgress()-1);
                            }

                            @Override
                            public void onFinish() {
                                firstText.setText(new StringBuilder(getString(R.string.not_yet_verification)));
                                progressBar.setVisibility(View.GONE);
                                canResend = true;
                                btnContinue.setText(getString(R.string.resend));
                                //firstText.setText("");
                                canVerify = false;
                            }
                        }.start();
                        sendVerificationCode(phone);
                    } else
                        Toast.makeText(PhoneAuth.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                } else if (canResend) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        canResend = false;
                        btnContinue.setText(R.string.verify);
                        canVerify = true;
                        sendVerificationCode(phone);
                        new CountDownTimer(60000 * ++interval, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (isJobDone)
                                    cancel();
                                firstText.setText(new StringBuilder(getString(R.string.arrival_count_down) + " " + (millisUntilFinished / 1000)) );
                            }

                            @Override
                            public void onFinish() {
                                canResend = true;
                                canVerify = false;
                                btnContinue.setText(R.string.resend);
                            }
                        }.start();
                    } else
                        Toast.makeText(PhoneAuth.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

                } else if (canVerify) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        String verificationCode = Objects.requireNonNull(pinView.getText()).toString();
                        if (verificationCode.isEmpty()) {
                            //Toast.makeText(PhoneAuth.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                        } else {
                            mDialog.setMessage("Please Wait...");
                            mDialog.show();
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                            signInWithPhoneAuthCredential(credential);
                        }
                    } else
                        Toast.makeText(PhoneAuth.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(PhoneAuth.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;

                // ...
            }
        };
    }

    @Override
    public void onBackPressed() {
        final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(PhoneAuth.this);
        alertDialog.setTitle(R.string.cance_verification);
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(PhoneAuth.this, MainActivity.class));
                finish();
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();

    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,               // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneAuth.this,// Activity (for callback binding)
                mCallbacks);// OnVerificationStateChangedCallbacksd

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mDialog.setMessage(getResources().getString(R.string.processing));
        mDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            isJobDone = true;
                            //Toast.makeText(PhoneAuth.this, "Verification Succeed!", Toast.LENGTH_SHORT).show();
                            users.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    mDialog.dismiss();
//                                    FirebaseUser cursor = FirebaseAuth.getInstance().getCurrentUser();
//                                    assert cursor != null;
//                                    phone = cursor.getPhoneNumber();
                                    User user = new User(username, phone);
                                    if (status.equals("new")) {
                                        user.setBalance(0.0);
                                        user.setCreatedAt(new SimpleDateFormat
                                                ("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
                                        users.child(phone).setValue(user);
                                    }
                                    else
                                    {
                                        user.setBalance(users.child(phone).child("balance"));
                                        //user.setHomeAddress(users.child(phone).child("j").toString());
                                    }
                                    //users.child(phone).setValue(user);
                                    Common.currentUser = user;
                                    //Toast.makeText(PhoneAuth.this, "Signed in Successfully !", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(PhoneAuth.this, Home.class));
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });
                        } else {

                            mDialog.dismiss();
                            Toast.makeText(PhoneAuth.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            Log.w("PhoneAuthError", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(PhoneAuth.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
