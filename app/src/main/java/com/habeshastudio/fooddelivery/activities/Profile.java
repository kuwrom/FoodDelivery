package com.habeshastudio.fooddelivery.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.habeshastudio.fooddelivery.BuildConfig;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.activities.profile.About;
import com.habeshastudio.fooddelivery.activities.profile.HistoryAndReceipt;
import com.habeshastudio.fooddelivery.activities.profile.PromoCodes;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.User;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Profile extends AppCompatActivity {

    DatabaseReference users, feedback;
    StorageReference storageReference;
    FirebaseStorage storage;
    TextView itemsCount, priceTag;
    LinearLayout checkoutButton;
    String isSubscribed;
    LinearLayout  paymentMethod, promoCode, transactions, share, help;
    Button about, loved, promo, feedBack;
    TextView name_display, address_display, balance_display;
    CircularImageView  profile;
    boolean isUsd, isLanguage, isNightMode;
    FloatingActionButton notificationSwitch, languageSwitch, nightModeSwitch, currencySwitch;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    //Url
    Uri saveUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rf.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        name_display = findViewById(R.id.name_display);
        profile = findViewById(R.id.profile_pic);
        address_display = findViewById(R.id.address_display);
        balance_display = findViewById(R.id.balance_display);
        users = FirebaseDatabase.getInstance().getReference("User");
        feedback = FirebaseDatabase.getInstance().getReference("Feedback");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Paper.init(Profile.this);
        //Refresh user status
        loadUser();

        promo = findViewById(R.id.promo_button);
        loved = findViewById(R.id.loved_button);

        //fab buttons
        notificationSwitch = findViewById(R.id.notification_switch);
        languageSwitch =  findViewById(R.id.language_switch);
        nightModeSwitch =  findViewById(R.id.easy_mode_switch);
        currencySwitch =  findViewById(R.id.currency_switch);
        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

        paymentMethod = findViewById(R.id.btnPaymentMethod);
        promoCode = findViewById(R.id.btnPromoCode);
        transactions = findViewById(R.id.btnTransactions);
        share = findViewById(R.id.btnShare);
        help = findViewById(R.id.btn_help);
        about = findViewById(R.id.btn_about);
        feedBack = findViewById(R.id.btn_feedback);
        checkoutButton =findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(Profile.this, Cart.class);
                startActivity(cartIntent);
            }
        });

        loved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, FavoritesActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        promo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, PromoCodes.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Dine");
                    String shareMessage= "\nDine is an amazing Food Delivery app. Check it out\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        final BubbleNavigationLinearView bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {
                    startActivity(new Intent(Profile.this, Home.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 1) {
                    startActivity(new Intent(Profile.this, OrderStatus.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 2) {
                    startActivity(new Intent(Profile.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 3) {
                    startActivity(new Intent(Profile.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 4) {

                } else {

                }
            }
        });

        refreshStatus();

        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSubscribed = Paper.book().read("sub_new");
                if ((isSubscribed == null || TextUtils.isEmpty(isSubscribed) || isSubscribed.equals("false"))) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    Paper.book().write("sub_new", "true");
                    notificationSwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_bg_light)));
                    //Toast.makeText(Profile.this, "tuned on", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                    Paper.book().write("sub_new", "false");
                    notificationSwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
                    //Toast.makeText(Profile.this, "tuned off", Toast.LENGTH_SHORT).show();
                }
            }
        });
        currencySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Paper.book().read("usd")!=null)
                isUsd = Paper.book().read("usd");
                if (isUsd) {
                    Common.isUsdSelected = false;
                    Paper.book().write("usd", false);
                    currencySwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
                    setCartStatus();
                } else {
                    Common.isUsdSelected = true;
                    Paper.book().write("usd", true);
                    currencySwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_bg_light)));
                    setCartStatus();
                }
            }
        });


        paymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chanePaymentMethod();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.startAnimation(animShake);
                final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
                alertDialog.setTitle("Edit Profile Picture?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chooseImage();
                    }
                });
                alertDialog.show();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, About.class));
            }
        });
        feedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackDialog();
            }
        });

        transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, HistoryAndReceipt.class));
            }
        });

        promoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeAddressDialog();
            }
        });

        setCartStatus();

    }

    private void refreshStatus() {


        //notification
        isSubscribed = Paper.book().read("sub_new");
        if (Paper.book().read("usd")!=null)
        isUsd = Paper.book().read("usd");

        if (isSubscribed == null || TextUtils.isEmpty(isSubscribed) || isSubscribed.equals("false"))
            notificationSwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
        else notificationSwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_bg_light)));

        if (isUsd)
            currencySwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_bg_light)));
        else currencySwitch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));



    }

    private void changeCurrency() {
    }

    private void chanePaymentMethod() {
    }

    private void chaneLanguage() {

    }

    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle("Settings");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_settings = inflater.inflate(R.layout.setting_layout, null);
        final CheckBox checkBox_news = layout_settings.findViewById(R.id.ckb_sub_news);
        Paper.init(this);
        String isSubscribed = Paper.book().read("sub_new");
        if (isSubscribed == null || TextUtils.isEmpty(isSubscribed) || isSubscribed.equals("false"))
            checkBox_news.setChecked(false);
        else checkBox_news.setChecked(true);
        alertDialog.setView(layout_settings);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (checkBox_news.isChecked()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    Paper.book().write("sub_new", "true");
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                    Paper.book().write("sub_new", "false");
                }
            }
        });
        alertDialog.show();
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle("Delivery Address");
        alertDialog.setMessage("Please Enter your address");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.home_address_layout, null);
        final MaterialEditText editHomeAddress = layout_home_address.findViewById(R.id.editHomeAddress);
        alertDialog.setView(layout_home_address);

        alertDialog.setPositiveButton("SET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Set new Home Address
                Common.currentUser.setHomeAddress(editHomeAddress.getText().toString());
                users.child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Profile.this, "Home address Successfully Updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        });
        alertDialog.show();
    }
    private void showHelpDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle("Help");
        alertDialog.setMessage("Dine app navigation and usage");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.help, null);
        alertDialog.setView(layout_home_address);
        alertDialog.show();
    }
    private void showFeedbackDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle("Feedback");
        alertDialog.setMessage("Type in your feedback below");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.feedback_layout, null);
        final MaterialEditText editHomeAddress = layout_home_address.findViewById(R.id.edit_feedback);
        alertDialog.setView(layout_home_address);

        alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Set new Home Address
                Common.currentUser.setHomeAddress(editHomeAddress.getText().toString());
                feedback.child(Common.currentUser.getPhone()).child(String.valueOf(System.currentTimeMillis()))
                        .setValue(editHomeAddress.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Profile.this, "Feedback Successfully Submitted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        });
        alertDialog.show();
    }

    void loadUser() {
        FirebaseDatabase.getInstance().getReference("User")
                .child(Common.currentUser.getPhone())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Common.currentUser = dataSnapshot.getValue(User.class);
                        name_display.setText(Common.currentUser.getName());
                        balance_display.setText(Common.currentUser.getBalance().toString());
                        if (!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) ||
                                Common.currentUser.getHomeAddress() != null) {
                            address_display.setText(Common.currentUser.getHomeAddress());
                        }
                        try {
                            Picasso.with(getBaseContext()).load(Common.currentUser.getImage())
                                    .into(profile);
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Please fill in all info");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout, null);
        final MaterialEditText editPassword = layout_pwd.findViewById(R.id.editPassword);
        final MaterialEditText editNewPassword = layout_pwd.findViewById(R.id.editNewPassword);
        final MaterialEditText editConfirmPassword = layout_pwd.findViewById(R.id.editConfirmPassword);

        alertDialog.setView(layout_pwd);
        alertDialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Change password Here
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Profile.this);
                waitingDialog.show();

                //Check old Password
                if (editPassword.getText().toString().equals(Common.currentUser.getPassword())) {
                    //check password match
                    if (editNewPassword.getText().toString().equals(editConfirmPassword.getText().toString())) {
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", editNewPassword.getText().toString());
                        //Commit Update
                        users.child(Common.currentUser.getPhone()).updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Profile.this, "Password Successfully Updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(Profile.this, "Passwords Doesn't match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(Profile.this, "Wrong old Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            final StorageReference imageFolder = storageReference.child("images/profile/"+Common.currentUser.getPhone());
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Profile.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //uri.toString()
                                    users.child(Common.currentUser.getPhone()).child("Image").setValue(uri.toString());
                                    loadUser();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(Profile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploading "+String.format("%.0f", (progress))+" %");
                        }
                    });
        }
    }
    // Dispatch incoming result to the correct fragment.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 71 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();
            uploadImage();
            //selected image ready to upload
        }
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Choose Image"),71);
    }

    public void setCartStatus(){
        priceTag = findViewById(R.id.checkout_layout_price);
        itemsCount = findViewById(R.id.items_count);
        int totalCount = new Database(this).getCountCart(Common.currentUser.getPhone());
        if (totalCount == 0)
            checkoutButton.setVisibility(View.GONE);
        else{
            checkoutButton.setVisibility(View.VISIBLE);
            itemsCount.setText(String.valueOf(totalCount));
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            if (Common.isUsdSelected)
                priceTag.setText(fmt.format(total/Common.ETB_RATE));
            else priceTag.setText(String.format("ETB %s", total));
            //priceTag.setText(fmt.format(total));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Common.currentUser.getPhone() == null)
            if (Paper.book().read("userPhone") != null)
                Common.currentUser.setPhone(Paper.book().read("userPhone").toString());
            else finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setCartStatus();
    }
}
