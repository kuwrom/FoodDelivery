package com.habeshastudio.fooddelivery.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.common.Config;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.helper.RecyclerItemTouchHelper;
import com.habeshastudio.fooddelivery.interfaces.RecyclerItemTouchHelperListener;
import com.habeshastudio.fooddelivery.models.DataMessage;
import com.habeshastudio.fooddelivery.models.MyResponse;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.Request;
import com.habeshastudio.fooddelivery.models.Token;
import com.habeshastudio.fooddelivery.models.User;
import com.habeshastudio.fooddelivery.remote.APIService;
import com.habeshastudio.fooddelivery.remote.IGoogleService;
import com.habeshastudio.fooddelivery.viewHolder.CartAdapter;
import com.habeshastudio.fooddelivery.viewHolder.CartViewHolder;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {

    private static final int PAYPAL_REQUEST_CODE = 9999;
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;
    //paypal integration
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    public TextView txtTotalPrice, subTotalView, taxView, deliveryFeeView, paymentMethodDisplay, addPromoText, guide;
    public int currentDeliveryPrice;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests, record;
    DatabaseReference users;
    DatabaseReference restaurant;
    Button checkout_button;
    LinearLayout btnPromoCode, addMore;
    RelativeLayout btnCashChange;
    int paymentMethodSelected = 0;
    EditText addComment, addPromo;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    APIService mService;
    String address, comment;
    Place shippingAddress;
    ProgressDialog mDialog;
    //Google Map API Retrofit
    IGoogleService mGoogleMapService;
    RelativeLayout rootLayout;
    private LocationManager mLocationManager;
    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


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
        setContentView(R.layout.activity_cart);

        mGoogleMapService = Common.getGoogleMapApi();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        //Runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //init paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        //Firebase
        database = FirebaseDatabase.getInstance();
        restaurant = database.getReference("Category");
        users = FirebaseDatabase.getInstance().getReference("User");
        requests = database.getReference("Requests");
        record = database.getReference("ForTheRecord");

        //init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mService = Common.getFCMService();
        rootLayout = findViewById(R.id.root_cart_layout);
        Paper.init(Cart.this);

        //Toast.makeText(this, ServerValue.TIMESTAMP.get(".sv"), Toast.LENGTH_SHORT).show();
        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);

        txtTotalPrice = findViewById(R.id.total_text);
        addMore = findViewById(R.id.add_more_layout);
        addPromoText = findViewById(R.id.add_promo_text);
        addComment = findViewById(R.id.add_note);
        addPromo = findViewById(R.id.enter_promo);
        subTotalView = findViewById(R.id.subtotal);
        paymentMethodDisplay = findViewById(R.id.payment_method_display);
        taxView = findViewById(R.id.tax);
        deliveryFeeView = findViewById(R.id.delivery_fee);
        btnPromoCode = findViewById(R.id.btnPromoCode);
        btnCashChange = findViewById(R.id.btnCashChange);
        checkout_button = findViewById(R.id.checkout_layout);
        guide = findViewById(R.id.cart_guide_text);

        Paper.init(Cart.this);
        btnPromoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPromo.setVisibility(View.VISIBLE);
                addPromoText.setText(getString(R.string.apply));
            }
        });
        btnCashChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (paymentMethodSelected == 0) {
//                    paymentMethodDisplay.setText(getString(R.string.pay_wiz_paypal));
//                    paymentMethodDisplay.setTextColor(getResources().getColor(R.color.blue_active));
//                    paymentMethodSelected = 1;
//                } else if (paymentMethodSelected == 1) {
//                    paymentMethodDisplay.setText(getString(R.string.pay_with_app_balence));
//                    paymentMethodDisplay.setTextColor(getResources().getColor(R.color.orange_active));
//                    paymentMethodSelected = 2;
//                } else if (paymentMethodSelected == 2) {
//                    paymentMethodDisplay.setText(getString(R.string.cod));
//                    paymentMethodDisplay.setTextColor(getResources().getColor(R.color.grey_active));
//                    paymentMethodSelected = 0;
//                }
                Toast.makeText(Cart.this, "Temporarily Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        checkout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showGpsDisabledDialog();
                } else if (cart.size() > 0)
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        showAlertDialog();
                    } else {
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    }

                else
                    Toast.makeText(Cart.this, getResources().getString(R.string.empty_cart), Toast.LENGTH_SHORT).show();
            }
        });


        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog();
        }
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.calculating_total));
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        if (Paper.book().read("cartGuide") == null)
            guide.setVisibility(View.VISIBLE);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "Sorry, Device not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle(getResources().getString(R.string.one_more_step));
        alertDialog.setMessage(getResources().getString(R.string.confirm_order));

        final LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);
        // final MaterialEditText editAddress = order_address_comment.findViewById(R.id.editAddress);
        final PlaceAutocompleteFragment editAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //hide search icon before places fragment
        editAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //set hint for Auto Complete
        editAddress.getView().findViewById(R.id.place_autocomplete_search_input)
                .setVisibility(View.GONE);
        //.setHint("Tap to select Different Address");
        //set text Size
        ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        //get address from places autocomplete
        editAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e("Error", status.getStatusMessage());
            }
        });

        //radio Choice
        final RadioButton rdiShipToAddress = order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = order_address_comment.findViewById(R.id.rdiHomeAddress);

        //Event Radio


        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) ||
                            Common.currentUser.getHomeAddress() != null) {
                        address = Common.currentUser.getHomeAddress();
                        ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                    } else {
                        Toast.makeText(Cart.this, "Please set your Home address", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Cart.this, Profile.class));
                        finish();
                    }
                }
            }
        });
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //ship to current address
                if (isChecked) {
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //if Api fetching is OK
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body());
                                        JSONArray resultsArray = jsonObject.getJSONArray("results");
                                        JSONObject firstObject = resultsArray.getJSONObject(0);
                                        address = firstObject.getString("formatted_address");
                                        //set this address to edit address
                                        ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Show paypal to payment
                ////////////////////////////////////////////////////////////////
                if (!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) ||
                        Common.currentUser.getHomeAddress() != null) {
                    address = Common.currentUser.getHomeAddress();
                    ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);

                }
                ////////////////////////////////////////////////////////////////

                //first get address and comment from dialog
                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {
                    if (shippingAddress != null)
                        address = shippingAddress.getAddress().toString();
                    else {
                        Toast.makeText(Cart.this, "Please Enter or Select your Address", Toast.LENGTH_SHORT).show();

                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }
                if (TextUtils.isEmpty(address)) {
                    address = "Not set yet";
//                    return;
                }

                comment = addComment.getText().toString();

                //Check Payment
                if (paymentMethodSelected == 1) {  //papal

                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("$", "")
                            .replace(",", "");
                    String formatAmountETB = txtTotalPrice.getText().toString()
                            .replace("ETB", "")
                            .replace(" ", "");
                    double amount;
                    if (Common.isUsdSelected)
                        amount = Double.parseDouble(formatAmount);
                    else amount = Double.parseDouble(formatAmountETB) / Common.ETB_RATE;
                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(amount)
                            , "USD", "Dine Food Delivery", PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                } else if (paymentMethodSelected == 2) {//app balance
                    double amount = 0.0;
                    try {
                        amount = Common.formatCurrency(txtTotalPrice.getText().toString(), Locale.US).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {
                        // Create new Request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                "Paid",
                                "App Balance",
                                String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                //String.format("%s,%s", 13.501090, 39.475850),
                                cart,
                                false,
                                Common.currentrestaurantID,
                                ""
                        );

                        // Submit to Firebase
                        //use System.CurrentMilli to Key
                        final String orderNumber = String.valueOf(ServerValue.TIMESTAMP);
                        requests.child(orderNumber)
                                .setValue(request);
                        //Delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        //Update Balance
                        double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
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
                                                            sendNotificationOrder(orderNumber, "");
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });
                        sendNotificationOrder(orderNumber, "");
                        //Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Cart.this, OrderStatus.class));
                        Common.currentrestaurantID = null;
                        Paper.book().delete("restId");
                        Common.alreadyBeenToCart = false;
                        Paper.book().delete("beenToCart");
                        finish();
                    } else {
                        Toast.makeText(Cart.this, getResources().getString(R.string.no_enough_balance), Toast.LENGTH_SHORT).show();
                    }

                } else if (paymentMethodSelected == 0) {
                    // Create new Request
                    FirebaseDatabase.getInstance().getReference("Category").child(Common.currentrestaurantID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Request request = new Request(
                                    Common.currentUser.getPhone(),
                                    Common.currentUser.getName(),
                                    address,
                                    txtTotalPrice.getText().toString() + " [D: " + deliveryFeeView.getText().toString() + "]",
                                    "0",
                                    comment,
                                    "Unpaid",
                                    "COD",
                                    String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                    cart,
                                    false,
                                    Common.currentrestaurantID,
                                    dataSnapshot.child("orderHandler").getValue().toString()
                            );

                            // Submit to Firebase
                            //use System.CurrentMilli to Key
                            String orderNumber = String.valueOf(System.currentTimeMillis());
                            requests.child(orderNumber)
                                    .setValue(request);
                            record.child(orderNumber).setValue(request);
                            sendNotificationOrder(orderNumber, dataSnapshot.child("orderHandler").getValue().toString());
                            //Delete cart
                            new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());


                            //Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Cart.this, OrderStatus.class));
                            Common.currentrestaurantID = null;
                            Paper.book().delete("restId");
                            Common.alreadyBeenToCart = false;
                            Paper.book().delete("beenToCart");
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                //Remove places fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();


            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Remove places fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();


            }
        });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);


                        // Create new Request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                jsonObject.getJSONObject("response").getString("state"),
                                "PayPal",
                                String.format("%s,%s", shippingAddress.getLatLng().latitude, shippingAddress.getLatLng().longitude),
                                cart,
                                false,
                                Common.currentrestaurantID,
                                ""
                        );

                        // Submit to Firebase
                        //use System.CurrentMilli to Key
                        String orderNumber = String.valueOf(System.currentTimeMillis());
                        requests.child(orderNumber)
                                .setValue(request);
                        //Delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        sendNotificationOrder(orderNumber, "");
                        //Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Cart.this, OrderStatus.class));
                        Common.currentrestaurantID = null;
                        Paper.book().delete("restId");
                        Common.alreadyBeenToCart = false;
                        Paper.book().delete("beenToCart");
                        finish();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == Activity.RESULT_CANCELED)
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
            else if (requestCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid Payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationOrder(final String orderNumber, String orderHandler) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.child(orderHandler).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Token serverToken = dataSnapshot.getValue(Token.class);

                    //create raw payload to send
//                    Notification notification = new Notification("Derash", "You have a new order" + orderNumber);
//                    Sender content = new Sender(serverToken.getToken(), notification);
                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", "Dine");
                    dataSend.put("message", "You have a new order" + orderNumber);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(), dataSend);

                    String test = new Gson().toJson(dataMessage);
                    Log.d("Connect", test);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            //Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Cart.this, OrderStatus.class));
                                            Common.currentrestaurantID = null;
                                            Paper.book().delete("restId");
                                            Common.alreadyBeenToCart = false;
                                            Paper.book().delete("beenToCart");
                                            finish();
                                        } else {
                                            //Toast.makeText(Cart.this, "...", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculate total price
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        if (Common.isUsdSelected)
            txtTotalPrice.setText(fmt.format((total + currentDeliveryPrice) / Common.ETB_RATE));
        else txtTotalPrice.setText(String.format("ETB %s", total + currentDeliveryPrice));
        updatePriceTexts();
        if (total == 0) {
            Common.currentrestaurantID = null;
            Paper.book().delete("restId");
            Common.alreadyBeenToCart = false;
            Paper.book().delete("beenToCart");
            finish();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {

        cart.remove(position);
        new Database(this).cleanCart(Common.currentUser.getPhone());
        for (Order item : cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Common.currentUserLocation = mLastLocation;
            //Common.currentRestaurantLocation = mLastLocation;
            Log.d("LOCATION", "Your location: " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
            calculateTotalPrice();
        } else {
            Log.d("LOCATION", "Couldn't get your Location");
            Toast.makeText(this, "Couldn't get your Location", Toast.LENGTH_SHORT).show();
            mDialog.dismiss();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            String name = deleteItem.getProductName();
            final int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());
            Paper.book().write("cartGuide", true);
            //update total
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            if (Common.isUsdSelected)
                txtTotalPrice.setText(fmt.format((total + currentDeliveryPrice) / Common.ETB_RATE));
            else txtTotalPrice.setText(String.format("ETB %s", total + currentDeliveryPrice));
            calculateTotalPrice();
            //make snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " " + getResources().getString(R.string.removed_from_cart), Snackbar.LENGTH_LONG);
            snackbar.setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //update total
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    if (Common.isUsdSelected)
                        txtTotalPrice.setText(fmt.format((total + currentDeliveryPrice) / Common.ETB_RATE));
                    else
                        txtTotalPrice.setText(String.format("ETB %s", total + currentDeliveryPrice));
                    calculateTotalPrice();
                }


            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.addCallback(new Snackbar.Callback() {

                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        int total = 0;
                        List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                        for (Order item : orders)
                            total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                        if (total == 0) {
                            Common.currentrestaurantID = null;
                            Paper.book().delete("restId");
                            Common.alreadyBeenToCart = false;
                            Paper.book().delete("beenToCart");
                            finish();
                        }
                    }
                }
            });
            snackbar.show();
        }
    }

    public void calculateTotalPrice() throws NullPointerException {


        restaurant.child(Common.currentrestaurantID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.child("deliveryPrice").getValue() == null)
                        currentDeliveryPrice = Common.getDeliveryPrice("20&10");
                    else
                        currentDeliveryPrice = Common.getDeliveryPrice(dataSnapshot.child("deliveryPrice").getValue().toString());
                    loadListFood();
                    updatePriceTexts();
                    mDialog.dismiss();
                } catch (Exception e) {
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    Common.currentrestaurantID = null;
                    Paper.book().delete("restId");
                    Common.alreadyBeenToCart = false;
                    Paper.book().delete("beenToCart");
                    finish();
                    //Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void updatePriceTexts() {
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        String formatAmount = txtTotalPrice.getText().toString()
                .replace("$", "")
                .replace(",", "");
        String formatAmountEtb = txtTotalPrice.getText().toString()
                .replace("ETB", "")
                .replace(" ", "")
                .replace(",", "");
        double amount;
        double subTotal;
        if (Common.isUsdSelected) {
            amount = Double.parseDouble(formatAmount);
            subTotal = (amount - (currentDeliveryPrice / Common.ETB_RATE)) / 1.15;
            double tax = subTotal * 0.15;
            deliveryFeeView.setText(fmt.format(currentDeliveryPrice / Common.ETB_RATE));
            subTotalView.setText(fmt.format(subTotal));
            taxView.setText(fmt.format(tax));
        } else {
            amount = Double.parseDouble(formatAmountEtb);
            subTotal = (amount - currentDeliveryPrice) / 1.15;
            double tax = subTotal * 0.15;
            deliveryFeeView.setText(String.format("ETB %s", currentDeliveryPrice));
            subTotalView.setText(String.format("ETB %s", (int) subTotal));
            taxView.setText(String.format("ETB %s", (int) tax));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.child(Paper.book().read("userPhone").toString()).getValue(User.class);
                Common.currentUser = currentUser;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void showGpsDisabledDialog() {
        final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle(getResources().getString(R.string.gps_disabled));
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        });
        alertDialog.show();
    }
}
