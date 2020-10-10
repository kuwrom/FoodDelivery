package com.habeshastudio.fooddelivery.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.habeshastudio.fooddelivery.MainActivity;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.helper.EmptyRecyclerView;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.models.Banner;
import com.habeshastudio.fooddelivery.models.Category;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.Token;
import com.habeshastudio.fooddelivery.remote.APIService;
import com.habeshastudio.fooddelivery.remote.IGoogleService;
import com.habeshastudio.fooddelivery.viewHolder.RestaurantAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Home extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;


    FirebaseDatabase database;
    public ProgressDialog mDialog;
    DatabaseReference users;
    Query filteredRestaurant;

    //Google Map API Retrofit
    IGoogleService mGoogleMapService;
    private LocationManager mLocationManager;
    APIService mService;
    //Location
    ImageView filter;

    //static int largestValue = 0;

    public static String selectedFilter = "0";
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    DatabaseReference category, geoRef, geoRestRef, geoBannerRef, banners;
    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {

        @Override
        public void onKeyEntered(final String key, GeoLocation location) {
            Log.d("entered", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

            //retrieve the restaurant from the database with an async task
            if (!restaurantKeyList.contains(key))
                restaurantKeyList.add(key);
            category.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null) {
                        availableRestaurants.put(key, category);
                        //recycler_menu.getAdapter().notifyDataSetChanged();
                        loadMenu(selectedFilter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Error", "onCancelled() called with: databaseError = [" + databaseError + "]");
                    Log.w("Error", "onCancelled: ", databaseError.toException());
                }
            });
        }


        @Override
        public void onKeyExited(String key) {
            Log.d("Error", String.format("Key %s is no longer in the search area", key));
            availableRestaurants.remove(key);
            restaurantKeyList.remove(key);
            //recycler_menu.getAdapter().notifyDataSetChanged();
            updateList();
        }


        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Log.d("Log", String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));


        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };
    AppBarLayout appBarLayout;
    GeoQuery geoQuery, geoQueryBanner;
    LinearLayout checkoutButton;
    EmptyRecyclerView recycler_menu;
    RelativeLayout rootLayout;
    RecyclerView.LayoutManager layoutManager;
    HashMap<String, Banner> availableBanners = new HashMap<>();
    //    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    RestaurantAdapter adapter;
    BubbleNavigationLinearView bubbleNavigationLinearView;
    ChipGroup filterGroup;
    SwipeRefreshLayout swipeRefreshLayout;
    HashMap<String, Category> availableRestaurants = new HashMap<>();
    HashMap<String, Category> filteredRestaurants = new HashMap<>();
    //Slider
    //HashMap<String, String> image_list;
    SliderLayout mSlider;
    private final GeoQueryEventListener geoQueryEventListenerBanner = new GeoQueryEventListener() {

        @Override
        public void onKeyEntered(final String key, GeoLocation location) {
            Log.d("entered", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

            //retrieve the restaurant from the database with an async task
            if (!availableBanners.containsKey(key)) {
                banners.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Banner banner = dataSnapshot.getValue(Banner.class);
                        if (banner != null) {
                            availableBanners.put(key, banner);
                            //recycler_menu.getAdapter().notifyDataSetChanged();
                            setupSlider();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Error", "onCancelled() called with: databaseError = [" + databaseError + "]");
                        Log.w("Error", "onCancelled: ", databaseError.toException());
                    }
                });
            }


        }


        @Override
        public void onKeyExited(String key) {
            Log.d("Error", String.format("Key %s is no longer in the search area", key));
            availableBanners.remove(key);
            setupSlider();
        }


        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Log.d("Log", String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));


        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };
    private GeoFire geoFire, geoFireBanner;

    @Override
    protected void attachBaseContext(Context newBase) {
        //super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    private List<String> restaurantKeyList = new ArrayList<>();
    //CounterFab fab;
    TextView itemsCount, priceTag, filterLabel;

    private static Map<String, Category> sortByComparator(Map<String, Category> unsortMap, final boolean order) {

        List<Map.Entry<String, Category>> list = new LinkedList<Map.Entry<String, Category>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Category>>() {
            public int compare(Map.Entry<String, Category> o1,
                               Map.Entry<String, Category> o2) {
                if (order) {
                    return o1.getValue().getPriority() >= o2.getValue().getPriority() ? 1 : -1;
                } else {
                    return o2.getValue().getPriority() >= o1.getValue().getPriority() ? 1 : -1;
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Category> sortedMap = new LinkedHashMap<String, Category>();
        for (Map.Entry<String, Category> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rf.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_home);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        isInternet();
        //Init Firebase
        Paper.init(Home.this);
        database = FirebaseDatabase.getInstance();
        geoRef = database.getReference("CurrentUserLocation");
        geoRestRef = database.getReference("RerstaurantLocation");
        geoBannerRef = database.getReference("BannerLocation");
        //FirebaseDatabase.getInstance().getReference("confidential").child("mobileCards").child("5").child("587456678646").child("valid").setValue(true);
        geoFire = new GeoFire(geoRestRef);
        appBarLayout = findViewById(R.id.app_bar_layout);
        geoFireBanner = new GeoFire(geoBannerRef);
        //geoFireBanner.setLocation("-M8UgGHAWfcgr_5ueqlM", new GeoLocation(13.487189,39.471165));
        //geoFire.setLocation("-M8UgGHAWfcgr_5ueqlM", new GeoLocation(13.487189,39.471165));
        rootLayout = findViewById(R.id.container_home);
        users = FirebaseDatabase.getInstance().getReference("User");
        category = database.getReference("Category");
        banners = database.getReference("Banner");
        mGoogleMapService = Common.getGoogleMapApi();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        filterLabel = findViewById(R.id.selected_restaurant_label);
        filter = findViewById(R.id.filter_button);
        filterGroup = findViewById(R.id.filter_group);

        checkoutButton = findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });

//        users.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                User currentUser = dataSnapshot.child(Paper.book().read("userPhone").toString()).getValue(User.class);
//                Common.currentUser = currentUser;
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });


        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterGroup.getVisibility() != View.VISIBLE)
                    filterGroup.setVisibility(View.VISIBLE);
                else filterGroup.setVisibility(View.GONE);
            }
        });

        filterGroup.setSingleSelection(true);
        filterGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final ChipGroup chipGroup, int i) {
                Chip selectedOne = chipGroup.findViewById(i);

                int index = filterGroup.indexOfChild(selectedOne);
                if (selectedOne != null)
                    filterLabel.setText(selectedOne.getText());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chipGroup.setVisibility(View.GONE);
                    }
                }, 700);

//                new java.util.Timer().schedule(
//                        new java.util.TimerTask() {
//                            @Override
//                            public void run() {
//                                chipGroup.setVisibility(View.GONE);
//                            }
//                        },
//                        2000
//                );
                switch (index) {
                    case 0:
                        loadMenu("0");
                        selectedFilter = "0";
                        break;
                    case 1:
                        loadMenu("1");
                        selectedFilter = "1";
                        break;
                    case 2:
                        loadMenu("2");
                        selectedFilter = "2";
                        break;
                    case 3:
                        loadMenu("3");
                        selectedFilter = "3";
                        break;
                    case 4:
                        loadMenu("4");
                        selectedFilter = "4";
                        break;
                    case 5:
                        loadMenu("5");
                        selectedFilter = "5";
                        break;
                    case 6:
                        loadMenu("6");
                        selectedFilter = "6";
                        break;
                    case 7:
                        loadMenu("7");
                        selectedFilter = "7";
                        break;
                    case 8:
                        loadMenu("8");
                        selectedFilter = "8";
                        break;
                    case 9:
                        loadMenu("9");
                        selectedFilter = "9";
                        break;
                }

            }
        });

        bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {

                } else if (position == 1) {
                    startActivity(new Intent(Home.this, OrderStatus.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 2) {
                    startActivity(new Intent(Home.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 3) {
                    startActivity(new Intent(Home.this, Profile.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 4) {
                    startActivity(new Intent(Home.this, Profile.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {

                }
            }
        });

        isInternet();


//        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
//                .setQuery(category, Category.class)
//                .build();
//
//        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
//
//                viewHolder.txtMenuName.setText(model.getName());
//                Picasso.with(getBaseContext()).load(model.getImage())
//                        .into(viewHolder.imageView);
//                final Category clickItem = model;
//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View view, int position, boolean isLongClik) {
//                        //Get CategoryId and Send to new activity
//                        Intent intent = new Intent(Home.this, FoodList.class);
//                        intent.putExtra("CategoryId", adapter.getRef(position).getKey());
//                        startActivity(intent);
//                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                    }
//                });
//            }
//
//            @NonNull
//            @Override
//            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View itemView = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.menu_item, parent, false);
//                return new MenuViewHolder(itemView);
//            }
//        };


        swipeRefreshLayout = findViewById(R.id.homeSwipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    updateQuery(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    updateList();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
            }
        });


        //load menu for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu(selectedFilter);
                    //updateQuery(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
            }
        });

        //////checkout button///////////
        setCartStatus();

        ////////////////////////////////


        recycler_menu = findViewById(R.id.recycler_menu);
        //recycler_menu.setHasFixedSize(true);
        //layoutManager = new LinearLayoutManager(this);
        //recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);


        try {
            updateToken(FirebaseInstanceId.getInstance().getToken());
        } catch (Exception e) {

        }

        //setup Slider
        //setupSlider();

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
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.loading_restaurants));
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
//        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
//        } else {
//            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
//        }
//
//        mLocationManager.addGpsStatusListener(this);
//        if(Common.currentUser.getHomeAddress() == null || Common.currentUser.getHomeAddress().isEmpty()){
//            final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
//            alertDialog.setTitle("Set Delivery Address?");
//            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    startActivity(new Intent(Home.this, Profile.class));
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                    Toast.makeText(Home.this, "Please Set Your Home Address", Toast.LENGTH_SHORT).show();
//                }
//            });
//            alertDialog.show();
//        }

    }

    private void updateQuery(GeoLocation myLocation) {
        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(myLocation, 10);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQuery.setLocation(myLocation, 10);
        }
        if (geoQueryBanner == null) {
            geoQueryBanner = geoFireBanner.queryAtLocation(myLocation, 50);
            geoQueryBanner.addGeoQueryEventListener(geoQueryEventListenerBanner);
        } else {
            geoQueryBanner.setLocation(myLocation, 50);
        }
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

    private void setupSlider() {
        if (availableBanners.isEmpty())
            appBarLayout.setVisibility(View.GONE);
        else
            appBarLayout.setVisibility(View.VISIBLE);
        isInternet();
        mSlider = findViewById(R.id.slider);
        //image_list = new HashMap<>();
//        final DatabaseReference banners = database.getReference("Banner");
//        banners.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    Banner banner = postSnapshot.getValue(Banner.class);
//                    image_list.put(banner.getName() + "@@@" + banner.getId(), banner.getImage());
//                }
//                    String[] keySplit = key.split("@@@");
//                    String nameOffood = keySplit[0];
//                    String idOfFood = keySplit[1];

        for (String key : availableBanners.keySet()) {

            //crete slider
            final DefaultSliderView textSliderView = new DefaultSliderView(getBaseContext());
            textSliderView
                    .image(availableBanners.get(key).getImage())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
//                                    Intent intent = new Intent(Home.this, FoodDetail.class);
//                                    //send food id to food detail
//                                    intent.putExtras(textSliderView.getBundle());
//                                    startActivity(intent);
                        }
                    });
            //Add extra Bundle
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle().putString("Message", availableBanners.get(key).getMessage());
            mSlider.addSlider(textSliderView);
            //remove Event after finish
        }


//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


        mSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Left_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(15000);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        isInternet();
        super.onResume();
        Common.currentUser.setPhone(Paper.book().read("userPhone").toString());

        bubbleNavigationLinearView.setCurrentActiveItem(0);

        checkPermission();

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog();
        }

        setCartStatus();
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Paper.book().read("userPhone").toString()).setValue(data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();
        //mSlider.stopAutoCycle();
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            geoQuery = null;
        }
    }

    void updateList() {
        for (final String key : restaurantKeyList) {
            category.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null) {
                        availableRestaurants.put(key, category);
                        //recycler_menu.getAdapter().notifyDataSetChanged();
                        loadMenu(selectedFilter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Error", "onCancelled() called with: databaseError = [" + databaseError + "]");
                    Log.w("Error", "onCancelled: ", databaseError.toException());
                }
            });
        }
    }

    private void loadMenu(String selectedCategory) {
        isInternet();
        filteredRestaurants.clear();
        //largestValue = 0;

        if (mLastLocation != null) {
            for (Map.Entry<String, Category> cat : availableRestaurants.entrySet()) {
                if (cat.getValue().getCategory().contains(selectedCategory)) {
                    if (selectedCategory.equals("1")) {
                        Location temp = new Location("A");
                        String[] restaurantLatLng = cat.getValue().getLocation().split(",");
                        temp.setLatitude(Double.parseDouble(restaurantLatLng[0]));
                        temp.setLongitude(Double.parseDouble(restaurantLatLng[1]));
                        double distance = Common.currentUserLocation.distanceTo(temp);
                        if (distance <= 120)
                            filteredRestaurants.put(cat.getKey(), cat.getValue());
                    } else filteredRestaurants.put(cat.getKey(), cat.getValue());
                }
            }

            adapter = new RestaurantAdapter((HashMap<String, Category>) sortByComparator(filteredRestaurants, false), this);
            //adapter.startListening();
            recycler_menu.setAdapter(adapter);

            mDialog.dismiss();
            recycler_menu.setEmptyView(findViewById(R.id.empty_view_restaurant));
            swipeRefreshLayout.setRefreshing(false);

            //Animation
            //recycler_menu.getAdapter().notifyDataSetChanged();
            recycler_menu.scheduleLayoutAnimation();
        }

    }


    public void showGpsDisabledDialog() {
        final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle(getResources().getString(R.string.gps_disabled));
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        });
        alertDialog.show();
    }


    public void setCartStatus() {
        priceTag = findViewById(R.id.checkout_layout_price);
        itemsCount = findViewById(R.id.items_count);
        int totalCount = 0;
        if (Common.currentUser != null)
            totalCount = new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//        else if (Paper.book().read("userPhone") != null)
//            totalCount =new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//
        else {
            startActivity(new Intent(Home.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
        if (totalCount == 0)
            checkoutButton.setVisibility(View.GONE);
        else {
            checkoutButton.setVisibility(View.VISIBLE);
            itemsCount.setText(String.valueOf(totalCount));
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Paper.book().read("userPhone").toString());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            if (Common.isUsdSelected)
                priceTag.setText(fmt.format(total / Common.ETB_RATE));
            else priceTag.setText(String.format("ETB %s", total));
            //priceTag.setText(fmt.format(total));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            if (geoQueryBanner != null)
                geoQueryBanner.removeAllListeners();
            geoQuery = null;
            geoQueryBanner = null;
        }
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
    protected void onStart() {
        isInternet();
        super.onStart();
        setCartStatus();
        checkPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void checkPermission() {
        //Runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
//        GeoFire geoFire = new GeoFire(geoRef);
//        geoFire.setLocation(Paper.book().read("userPhone").toString(), new GeoLocation(location.getLatitude(), location.getLongitude()));
        updateQuery(new GeoLocation(location.getLatitude(), location.getLongitude()));
        Common.currentUserLocation = mLastLocation;
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

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
