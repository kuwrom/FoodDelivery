package com.habeshastudio.fooddelivery.activities.authentication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;

import java.util.Objects;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Register extends AppCompatActivity {

    public static String name, phone;
    EditText edtPhone, edtName;
    TextView btnTrouble, registerLable;
    FloatingActionButton btnSignMeUp;
    RelativeLayout rootLayout;
    ProgressDialog mDialog;
    FirebaseDatabase database;
    DatabaseReference users;
    AlertDialog dialog_verifying;
    boolean isNameEditing = false;

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
        setContentView(R.layout.activity_register);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            getWindow().setStatusBarColor(Color.WHITE);
//        }
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        Paper.init(Register.this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        registerLable = findViewById(R.id.register_label);
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
                String language = Paper.book().read("language");
                if (language.equals("en")) {
                    Paper.book().write("language", "am");
                    updateView((String) Paper.book().read("language"));
                } else {
                    Paper.book().write("language", "en");
                    updateView((String) Paper.book().read("language"));
                }
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout= inflater.inflate(R.layout.processing_dialog,null);
        AlertDialog.Builder show = new AlertDialog.Builder(Register.this);

        show.setView(alertLayout);
        show.setCancelable(true);
        dialog_verifying = show.create();
        //dialog_verifying.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language", "en");
        updateView((String) Paper.book().read("language"));
        btnSignMeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    if (!Objects.requireNonNull(edtPhone.getText()).toString().isEmpty() && Integer.parseInt(edtPhone.getText().toString()) > 900000000) {
                        phone = edtPhone.getText().toString().trim();
                        if (phone.startsWith("0"))
                            phone = "+251" + phone.substring(1);
                        else if (phone.startsWith("9"))
                            phone = "+251" + phone;
                        else return;
                        Intent phoneAuth = new Intent(Register.this, PhoneAuth.class);
                        phoneAuth.putExtra("phone", phone);
                        Paper.book().write("userPhone", phone);
                        startActivity(phoneAuth);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                        //Toast.makeText(Register.this, "Welcome back " + name, Toast.LENGTH_SHORT).show();

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

    private void updateView(String language) {
        Context context = LocaleHelper.setLocale(this, language);
        Resources resources = context.getResources();
        registerLable.setText(resources.getString(R.string.welcome_to_dine));
        edtPhone.setHint(resources.getString(R.string.phone_number_please));
        edtName.setHint(resources.getString(R.string.and_your_name));
        btnTrouble.setText(resources.getString(R.string.change_language));
    }

    void isInternet() {
        if (!Common.isConnectedToInternet(getBaseContext())) {
            final Snackbar snackbar = Snackbar.make(rootLayout, getResources().getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
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
