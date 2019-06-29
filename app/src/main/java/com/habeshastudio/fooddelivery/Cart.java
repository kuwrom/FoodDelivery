package com.habeshastudio.fooddelivery;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.common.Config;
import com.habeshastudio.fooddelivery.Database.Database;
import com.habeshastudio.fooddelivery.Helper.RecyclerItemTouchHelper;
import com.habeshastudio.fooddelivery.Interface.RecyclerItemTouchHelperListener;
import com.habeshastudio.fooddelivery.Model.DataMessage;
import com.habeshastudio.fooddelivery.Model.MyResponse;
import com.habeshastudio.fooddelivery.Model.Order;
import com.habeshastudio.fooddelivery.Model.Request;
import com.habeshastudio.fooddelivery.Model.Token;
import com.habeshastudio.fooddelivery.Model.User;
import com.habeshastudio.fooddelivery.Remote.APIService;
import com.habeshastudio.fooddelivery.Remote.IGoogleService;
import com.habeshastudio.fooddelivery.ViewHolder.CartAdapter;
import com.habeshastudio.fooddelivery.ViewHolder.CartViewHolder;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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
    public TextView txtTotalPrice;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    Button btnPlace;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    APIService mService;
    String address, comment;
    Place shippingAddress;
    //Google Map API Retrofit
    IGoogleService mGoogleMapService;
    RelativeLayout rootLayout;
    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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


        //Runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
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
        requests = database.getReference("Requests");

        //init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mService = Common.getFCMService();
        rootLayout = findViewById(R.id.root_cart_layout);

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is Empty", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();
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
        alertDialog.setTitle("One more Step...");
        alertDialog.setMessage("Enter your Address: ");

        final LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);
        // final MaterialEditText editAddress = order_address_comment.findViewById(R.id.editAddress);
        final PlaceAutocompleteFragment editAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //hide search icon before places fragment
        editAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //set hint for Auto Complete
        ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");
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
        final MaterialEditText editComment = order_address_comment.findViewById(R.id.editComment);

        //radio Choice
        final RadioButton rdiShipToAddress = order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiPayPal = order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiCOD = order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiBalance = order_address_comment.findViewById(R.id.rdiDerashBalance);

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

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Show paypal to payment

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
                    Toast.makeText(Cart.this, "Please Select Payment Method", Toast.LENGTH_SHORT).show();

                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }

                comment = editComment.getText().toString();

                //Check Payment
                if (!rdiCOD.isChecked() && !rdiPayPal.isChecked() && !rdiBalance.isChecked()) {
                    Toast.makeText(Cart.this, "Please Enter or Select your Address", Toast.LENGTH_SHORT).show();

                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                } else if (rdiPayPal.isChecked()) {

                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("$", "")
                            .replace(",", "");
                    float amount = Float.parseFloat(formatAmount);

                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount)
                            , "USD", "Derash App Order", PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                } else if (rdiBalance.isChecked()) {
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
                                cart,
                                false
                        );

                        // Submit to Firebase
                        //use System.CurrentMilli to Key
                        final String orderNumber = String.valueOf(System.currentTimeMillis());
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
                                            //Refresh User
                                            FirebaseDatabase.getInstance().getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            sendNotificationOrder(orderNumber);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(Cart.this, "You don't Have enough balance, please Choose Other Method", Toast.LENGTH_SHORT).show();
                    }

                } else if (rdiCOD.isChecked()) {
                    // Create new Request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",
                            comment,
                            "Unpaid",
                            "COD",
                            String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            cart,
                            false
                    );

                    // Submit to Firebase
                    //use System.CurrentMilli to Key
                    String orderNumber = String.valueOf(System.currentTimeMillis());
                    requests.child(orderNumber)
                            .setValue(request);
                    //Delete cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                    sendNotificationOrder(orderNumber);
                    Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                //Remove places fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();


            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                                false
                        );

                        // Submit to Firebase
                        //use System.CurrentMilli to Key
                        String orderNumber = String.valueOf(System.currentTimeMillis());
                        requests.child(orderNumber)
                                .setValue(request);
                        //Delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        sendNotificationOrder(orderNumber);
                        Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
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

    private void sendNotificationOrder(final String orderNumber) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapSot : dataSnapshot.getChildren()) {
                    Token serverToken = postSnapSot.getValue(Token.class);

                    //create raw payload to send
//                    Notification notification = new Notification("Derash", "You have a new order" + orderNumber);
//                    Sender content = new Sender(serverToken.getToken(), notification);
                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", "Derash");
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
                                            Toast.makeText(Cart.this, "Thank you, Order placed!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Failed!", Toast.LENGTH_SHORT).show();
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

        txtTotalPrice.setText(fmt.format(total));
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("LOCATION", "Your location: " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        } else {
            Log.d("LOCATION", "Couldn't get your Location");
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
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            String name = deleteItem.getProductName();
            final int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            //update total
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));

            //make snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
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
                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }
}
