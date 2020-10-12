package com.habeshastudio.fooddelivery.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.habeshastudio.fooddelivery.activities.profile.PromoCodes;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.models.Card;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.User;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;

public class Profile extends AppCompatActivity {

    DatabaseReference users, feedback;
    StorageReference storageReference;
    FirebaseStorage storage;
    ValueEventListener myListner;
    TextView itemsCount, priceTag;
    public ProgressDialog mDialog;
    LinearLayout checkoutButton, balanceWithdraw;
    String isSubscribed;
    LinearLayout paymentMethod, promoCode, transactions, share, help;
    Button about, loved, promo, feedBack;
    TextView voucher;
    CardView voucherCard;
    DatabaseReference myReference;
    TextView name_display, address_display, balance_display, moreOptions, textPaymentMethod, textDeliveryAddress, textTransactions, textShare, textHelp;
    CircularImageView profile;
    boolean isUsd, isAmharic;
    FloatingActionButton notificationSwitch, languageSwitch, nightModeSwitch, currencySwitch;

    @Override
    protected void attachBaseContext(Context newBase) {
        //super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    //Url
    Uri saveUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        name_display = findViewById(R.id.name_display);
        profile = findViewById(R.id.profile_pic);
        address_display = findViewById(R.id.address_display);
        balance_display = findViewById(R.id.balance_display);
        balanceWithdraw = findViewById(R.id.balance_withdraw);
        textPaymentMethod = findViewById(R.id.txt_payment);
        textDeliveryAddress = findViewById(R.id.txt_delivery);
        textTransactions = findViewById(R.id.txt_transactions);
        textShare = findViewById(R.id.txt_share);
        textHelp = findViewById(R.id.txt_help);
        moreOptions = findViewById(R.id.more_options);
        users = FirebaseDatabase.getInstance().getReference("User");
        users.child(Paper.book().read("userPhone").toString()).keepSynced(true);
        feedback = FirebaseDatabase.getInstance().getReference("Feedback");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Paper.init(Profile.this);


        //Refresh user status
        //loadUser();

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
        checkoutButton = findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(Profile.this, Cart.class);
                startActivity(cartIntent);
            }
        });

        balanceWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBalanceWithdrawDialogue();
            }
        });

        languageSwitch.setOnClickListener(new View.OnClickListener() {
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

        loved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("tel:" + "0976000070");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, uri);
                try {
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityNotFoundException) {
                    // TODO: place code to handle users that have no call application installed, otherwise the app crashes
                }
//                startActivity(new Intent(Profile.this, FavoritesActivity.class));
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                //Toast.makeText(Profile.this, "Sorry but we are providing printed receipts instead", Toast.LENGTH_SHORT).show();
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

                currencySwitch.startAnimation(animShake);
                final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
                alertDialog.setTitle(R.string.change_currency);
                alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Paper.book().read("usd") != null)
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
                alertDialog.show();
            }
        });


        paymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePaymentMethod();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.startAnimation(animShake);
                final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
                alertDialog.setTitle(getResources().getString(R.string.edit_profile));
                alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
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
                showAboutDialog();
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
                startActivity(new Intent(Profile.this, OrderHistory.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        promoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeAddressDialog();
            }
        });
        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language", "en");
        updateView((String) Paper.book().read("language"));

    }

    @SuppressLint("SetTextI18n")
    private void showBalanceWithdrawDialogue() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        //alertDialog.setTitle("Withdraw Money");

        if (myListner != null) {
            myReference.removeEventListener(myListner);
            myListner = null;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_balance = inflater.inflate(R.layout.balance_layout, null);
        Button buyCard = layout_balance.findViewById(R.id.btn_buy_a_card);
        final LinearLayout cardList = layout_balance.findViewById(R.id.card_list);
        TextView availableBalance = layout_balance.findViewById(R.id.available_display);
        Long balance = (Long) Common.currentUser.getBalance();

        //Anim button Card ///////////////////////////////////////////////////////////////////////////////////////
        RelativeLayout relativelayout = layout_balance.findViewById(R.id.relative_layout_id);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(relativelayout, "alpha", .7f, .1f);
        fadeOut.setDuration(500);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(relativelayout, "alpha", .1f, .7f);
        fadeIn.setDuration(500);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });
        mAnimationSet.start();
        /////////////////////////////////////////////////////

        availableBalance.setText("Available Balance: " + balance.intValue() + " birr");


        final TextView five, ten, fifteen, twentyFive, fifty, hundred;
        voucherCard = layout_balance.findViewById(R.id.voucher_card);
        five = layout_balance.findViewById(R.id.five_birr);
        ten = layout_balance.findViewById(R.id.ten_birr);
        fifteen = layout_balance.findViewById(R.id.fifteen_birr);
        twentyFive = layout_balance.findViewById(R.id.twenty_five_birr);
        fifty = layout_balance.findViewById(R.id.fifty_birr);
        hundred = layout_balance.findViewById(R.id.hundred_birr);
        voucher = layout_balance.findViewById(R.id.voucher_number);


        if (Common.currentUser.getCurrentMobileCard() != null && !Common.currentUser.getCurrentMobileCard().isEmpty()) {
            voucher.setText(Common.currentUser.getCurrentMobileCard());
            voucherCard.setVisibility(View.VISIBLE);
        }
        buyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardList.setVisibility(View.VISIBLE);
                if ((Long) Common.currentUser.getBalance() >= 5) five.setEnabled(true);
                else five.setEnabled(false);
                if ((Long) Common.currentUser.getBalance() >= 10) ten.setEnabled(true);
                else ten.setEnabled(false);
                if ((Long) Common.currentUser.getBalance() >= 15) fifteen.setEnabled(true);
                else fifteen.setEnabled(false);
                if ((Long) Common.currentUser.getBalance() >= 25) twentyFive.setEnabled(true);
                else twentyFive.setEnabled(false);
                if ((Long) Common.currentUser.getBalance() >= 50) fifty.setEnabled(true);
                else fifty.setEnabled(false);
                if ((Long) Common.currentUser.getBalance() >= 100) hundred.setEnabled(true);
                else hundred.setEnabled(false);

            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVoucher(5);
            }
        });
        ten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVoucher(10);
            }
        });
        fifteen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVoucher(15);
            }
        });
        twentyFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVoucher(25);
            }
        });
        fifty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVoucher(50);
            }
        });
        hundred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVoucher(100);
            }
        });
        voucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String voucherNumber = voucher.getText().toString();

                Uri uri = Uri.parse("tel:" + Uri.encode("*805*" + voucherNumber + "#"));
                Intent callIntent = new Intent(Intent.ACTION_DIAL, uri);
                try {
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityNotFoundException) {
                    // TODO: place code to handle users that have no call application installed, otherwise the app crashes
                }
            }
        });


        alertDialog.setView(layout_balance);
        alertDialog.show();
    }

    //todo before calling this set processing dialogue
    private void requestVoucher(final int amount) {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Requesting a Mobile Card");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle("This will cost you " + amount + " birr.");
        alertDialog.setMessage("Are you sure, do you want to continue?");
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //mDialog.dismiss();
            }
        });
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((Long) Common.currentUser.getBalance() >= amount) {

                    myReference = FirebaseDatabase.getInstance().getReference("confidential").child("mobileCards")
                            .child(String.valueOf(amount)).child("cards");
                    myReference.keepSynced(true);
                    myListner = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String cardNumber = String.valueOf(new Card(dataSnapshot).getList().get(0).get("cards"));

                                final Map<String, Object> update_balance = new HashMap<>();
                                final Map<String, Object> update_card = new HashMap<>();
                                final Map<String, Object> transactionHistory = new HashMap<>();
                                update_card.put("valid", false);
                                update_card.put("userPhone", Paper.book().read("userPhone").toString());
                                update_card.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                                transactionHistory.put("amount", amount);
                                transactionHistory.put("method", "mobile card");
                                transactionHistory.put("comments", cardNumber);
                                update_balance.put("currentMobileCard", cardNumber);

                                FirebaseDatabase.getInstance().getReference("confidential").child("mobileCards")
                                        .child(String.valueOf(amount)).child("cards").child(cardNumber).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                users.child(Paper.book().read("userPhone").toString()).child("balance")
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                double balance = Double.parseDouble(Objects.requireNonNull(dataSnapshot.getValue()).toString());

                                                                update_balance.put("balance", balance - amount);
                                                                users.child(Paper.book().read("userPhone").toString())
                                                                        .updateChildren(update_balance)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                transactionHistory.put("newBalance", Objects.requireNonNull(update_balance.get("balance")));
                                                                                String timeNow = String.valueOf(System.currentTimeMillis());
                                                                                FirebaseDatabase.getInstance().getReference("confidential").child("withdrawalHistory")
                                                                                        .child(Paper.book().read("userPhone").toString()).child(timeNow).updateChildren(transactionHistory)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        loadUser();
                                                                                        if (Common.currentUser.getCurrentMobileCard() != null)
                                                                                            voucher.setText(Common.currentUser.getCurrentMobileCard());
                                                                                        voucherCard.setVisibility(View.VISIBLE);
                                                                                        if (myListner != null)
                                                                                            myReference.removeEventListener(myListner);
                                                                                        mDialog.dismiss();

                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(Profile.this, "Sorry, out of Stock!", Toast.LENGTH_SHORT).show();
                                if (myListner != null)
                                    myReference.removeEventListener(myListner);
                                mDialog.dismiss();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    myReference.limitToFirst(1).addListenerForSingleValueEvent(myListner);


                } else {
                    mDialog.dismiss();
                    Toast.makeText(Profile.this, "Balance insufficient", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.show();
    }

    private void updateView(String language) {
        Context context = LocaleHelper.setLocale(this, language);
        Resources resources = context.getResources();
        loved.setText(resources.getString(R.string.call_us));
        promo.setText(resources.getString(R.string.promo));
        moreOptions.setText(resources.getString(R.string.more_options));

        textPaymentMethod.setText(resources.getString(R.string.setup_payment_method));
        textDeliveryAddress.setText(resources.getString(R.string.home_address));
        textTransactions.setText(resources.getString(R.string.history_and_transactions));
        textShare.setText(resources.getString(R.string.share_dine));
        textHelp.setText(resources.getString(R.string.help));

        about.setText(resources.getString(R.string.about));
        feedBack.setText(resources.getString(R.string.send_feedback));
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

    private void changePaymentMethod() {
        Toast.makeText(this, getResources().getString(R.string.payments_are_disabled), Toast.LENGTH_SHORT).show();
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle(getResources().getString(R.string.set_home_address));

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.home_address_layout, null);
        final MaterialEditText editHomeAddress = layout_home_address.findViewById(R.id.editHomeAddress);
        alertDialog.setView(layout_home_address);

        alertDialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Set new Home Address
                Common.currentUser.setHomeAddress(editHomeAddress.getText().toString());
                users.child(Paper.book().read("userPhone").toString())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                loadUser();
                                Toast.makeText(Profile.this, getResources().getString(R.string.home_success), Toast.LENGTH_SHORT).show();
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
        alertDialog.setTitle(getResources().getString(R.string.help));
        alertDialog.setMessage(getResources().getString(R.string.nav_usage));

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.help, null);
        alertDialog.setView(layout_home_address);
        alertDialog.show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.about_layout, null);
        alertDialog.setView(layout_home_address);
        alertDialog.show();
    }
    private void showFeedbackDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle(getResources().getString(R.string.feedback));
        alertDialog.setMessage(getResources().getString(R.string.feedback_below));

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.feedback_layout, null);
        final MaterialEditText editHomeAddress = layout_home_address.findViewById(R.id.edit_feedback);
        alertDialog.setView(layout_home_address);

        alertDialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Set new Home Address
                Common.currentUser.setHomeAddress(editHomeAddress.getText().toString());
                feedback.child(Paper.book().read("userPhone").toString()).child(String.valueOf(System.currentTimeMillis()))
                        .setValue(editHomeAddress.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Profile.this, getResources().getString(R.string.feedback_success), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myListner != null)
            myReference.removeEventListener(myListner);

    }

    void loadUser() {
        users.child(Paper.book().read("userPhone").toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Common.currentUser = dataSnapshot.getValue(User.class);
                assert Common.currentUser != null;
                name_display.setText(Common.currentUser.getName());
                balance_display.setText(Common.currentUser.getBalance().toString());
                if (!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) ||
                        Common.currentUser.getHomeAddress() != null) {
                    address_display.setText(Common.currentUser.getHomeAddress());
                }
                Picasso.with(getBaseContext()).load(Common.currentUser.getImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile_pic)
                                    .into(profile);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage(getResources().getString(R.string.processing));
            mDialog.show();

            final StorageReference imageFolder = storageReference.child("profile/" + Paper.book().read("userPhone").toString());
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Profile.this, getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                            final StorageReference imageMe = storageReference.child("profile/" + Paper.book().read("userPhone").toString() + "_512x512");
                            imageMe.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //uri.toString()
                                    users.child(Paper.book().read("userPhone").toString()).child("image").setValue(uri.toString());
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
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage(getResources().getString(R.string.processing) + " " + String.format("%.0f", (progress)) + " %");
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
        int totalCount = new Database(this).getCountCart(Paper.book().read("userPhone").toString());
        if (totalCount == 0)
            checkoutButton.setVisibility(View.GONE);
        else{
            checkoutButton.setVisibility(View.VISIBLE);
            itemsCount.setText(String.valueOf(totalCount));
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Paper.book().read("userPhone").toString());
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
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.child(Paper.book().read("userPhone").toString()).getValue(User.class);
                Common.currentUser = currentUser;
                loadUser();
                setCartStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //setCartStatus();
    }
}
