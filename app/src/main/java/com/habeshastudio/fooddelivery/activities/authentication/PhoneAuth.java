package com.habeshastudio.fooddelivery.activities.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.activities.Home;
import com.habeshastudio.fooddelivery.models.User;
import com.habeshastudio.fooddelivery.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {

    public String username, phone, status;
    TextView textTitle, firstText, phoneText, secondText;
    Button btnContinue;
    PinView pinView;
    boolean firstTime = true;
    int interval = 1;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        Intent intent = getIntent();
        username = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        status = intent.getStringExtra("status");
        //view
        textTitle = findViewById(R.id.auth_title_text);
        firstText = findViewById(R.id.auth_first_text);
        phoneText = findViewById(R.id.auth_Phone_label);
        secondText = findViewById(R.id.auth_second_text);
        btnContinue = findViewById(R.id.btnVerify);
        pinView = findViewById(R.id.pinView);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child("User");
        mDialog = new ProgressDialog(PhoneAuth.this);

        phoneText.setText(new StringBuilder(R.string.dear + phone));
        textTitle.setText(new StringBuilder(R.string.et_code + username));

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstTime) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        btnContinue.setText(getString(R.string.verify));
                        canVerify = true;
                        phoneText.setVisibility(View.GONE);
                        pinView.setVisibility(View.VISIBLE);
                        textTitle.setText(new StringBuilder(getString(R.string.code_sent_to) + phone));
                        secondText.setVisibility(View.GONE);
                        firstTime = false;
                        new CountDownTimer(60000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (isJobDone)
                                    cancel();
                                firstText.setText(new StringBuilder(getString(R.string.arrival_count_down) + (millisUntilFinished / 1000)) );
                            }

                            @Override
                            public void onFinish() {
                                canResend = true;
                                btnContinue.setText(getString(R.string.resend));
                                //firstText.setText("");
                                canVerify = false;
                            }
                        }.start();
                        sendVerificationCode(phone);
                    } else
                        Toast.makeText(PhoneAuth.this, "Sorry, We can't make it to the internet!", Toast.LENGTH_SHORT).show();
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
                                firstText.setText(new StringBuilder(getString(R.string.arrival_count_down) + (millisUntilFinished / 1000)) );
                            }

                            @Override
                            public void onFinish() {
                                canResend = true;
                                canVerify = false;
                                btnContinue.setText(R.string.resend);
                            }
                        }.start();
                    } else
                        Toast.makeText(PhoneAuth.this, "Please Check your Internet Connection", Toast.LENGTH_SHORT).show();

                } else if (canVerify) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        String verificationCode = Objects.requireNonNull(pinView.getText()).toString();
                        if (verificationCode.isEmpty()) {
                            Toast.makeText(PhoneAuth.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                        } else {
                            mDialog.setMessage("Please Wait...");
                            mDialog.show();
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                            signInWithPhoneAuthCredential(credential);
                        }
                    } else
                        Toast.makeText(PhoneAuth.this, "Please Check your Internet Connection", Toast.LENGTH_SHORT).show();
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

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneAuth.this,               // Activity (for callback binding)
                mCallbacks);// OnVerificationStateChangedCallbacks

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mDialog.setMessage("Verifying phone...");
        mDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            isJobDone = true;
                            Toast.makeText(PhoneAuth.this, "Verification Succeed!", Toast.LENGTH_SHORT).show();
                            users.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    mDialog.dismiss();
                                    FirebaseUser cursor = FirebaseAuth.getInstance().getCurrentUser();
                                    assert cursor != null;
                                    phone = cursor.getPhoneNumber();
                                    User user = new User(username, "");
                                    users.child(phone).setValue(user);
                                    user.setPhone(phone);
                                    if (status.equals("new")) {
                                        users.child(phone).child("Created at").setValue(new SimpleDateFormat
                                                ("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
                                        users.child(phone).child("status").setValue("normal");
                                    }
                                    Common.currentUser = user;
                                    Toast.makeText(PhoneAuth.this, "Signed in Successfully !", Toast.LENGTH_SHORT).show();
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
