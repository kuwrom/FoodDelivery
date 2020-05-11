package com.habeshastudio.fooddelivery.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.MainActivity;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.helper.EmptyRecyclerView;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;
import com.habeshastudio.fooddelivery.models.Favorites;
import com.habeshastudio.fooddelivery.models.Food;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.viewHolder.FoodViewHolder;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    EmptyRecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    TextView itemsCount, priceTag;
    LinearLayout checkoutButton;

    String categoryId = "";
    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    //Facebook Share
    CallbackManager callbackManager;


    Database localDb;
    ShareDialog shareDialog;
    //create target from picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo ke bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rf.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_food_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        //init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Firebase Init
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        checkoutButton =findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(FoodList.this, Cart.class);
                startActivity(cartIntent);
            }
        });
        localDb = new Database(this);

        final BubbleNavigationLinearView bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 1) {
                    startActivity(new Intent(FoodList.this, OrderStatus.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 2) {
                    startActivity(new Intent(FoodList.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 3) {
                    startActivity(new Intent(FoodList.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 4) {
                    startActivity(new Intent(FoodList.this, Profile.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {

                }
            }
        });
        bubbleNavigationLinearView.setCurrentActiveItem(3);

        swipeRefreshLayout = findViewById(R.id.foodSwipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(FoodList.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Common.currentRestaurantLocation = Common.restaurantDistance.get(getIntent().getStringExtra("CategoryId"))
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(FoodList.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    }
                }
                //Search
                materialSearchBar = findViewById(R.id.searchBar);
                materialSearchBar.setHint(getResources().getString(R.string.search));
                loadSuggest();//function to load suggestion from firebase
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        List<String> suggest = new ArrayList<String>();
                        for (String search : suggestList) {
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
//        View emptyView = findViewById(R.id.empty_view);
//        recyclerView.setEmptyView(emptyView);
        TextView view = new TextView(FoodList.this);
        //view.setText("No items available");
        recyclerView.setEmptyView(view);

        setCartStatus();
    }

    private void startSearch(CharSequence text) {
        //create query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        //now create options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, int position, @NonNull Food model) {


                viewHolder.food_name.setText(model.getName());
                Log.d("TAG", "" + adapter.getItemCount());
                Picasso.with(getBaseContext()).load(model.getImage()).placeholder(R.drawable.foodbg)
                        .into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Start New Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey()); //Send food Id to new activity
                        viewHolder.food_image.setTransitionName("thumbnailTransition");
                        Pair<View, String> pair1 = Pair.create((View) viewHolder.food_image, viewHolder.food_image.getTransitionName());
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(FoodList.this, pair1);
                        FoodList.this.startActivity(foodDetail, optionsCompat.toBundle());
                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());
                        }
                        materialSearchBar.setLastSuggestions(suggestList);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(final String categoryId) {

        //create query by category Id
        Query searchByName = foodList.orderByChild("menuId").equalTo(categoryId);
        //now create options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {

                viewHolder.food_name.setText(model.getName());
                viewHolder.food_description.setText(model.getDescription());
                if (Common.isUsdSelected)
                viewHolder.food_price.setText(String.format("$ %s", Integer.parseInt(model.getPrice())/Common.ETB_RATE));
                else viewHolder.food_price.setText(String.format("ETB %s", model.getPrice()));
                Log.d("TAG", "" + adapter.getItemCount());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .placeholder(R.drawable.foodbg)
                        .into(viewHolder.food_image);

                //quick cart
                final boolean isAddedToCart = new Database(getBaseContext()).isAddedToCart(adapter.getRef(position).getKey(), Common.currentUser.getPhone());

                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isAddedToCart) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()

                            ));
                            setCartStatus();
                            Toast.makeText(FoodList.this, "Added to cart", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FoodList.this, "Food already added to cart", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                //Add favourites
                if (localDb.isFavourite(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());


                        if (!localDb.isFavourite(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDb.addToFavourites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            //Toast.makeText(FoodList.this, "" + model.getName() + " was added to favourites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDb.removeFromFavourites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            //Toast.makeText(FoodList.this, "" + model.getName() + " was removed from favourites", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Start New Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey()); //Send food Id to new activity
                        Common.proposedrestaurantID = model.getMenuId();
                        //foodDetail.putExtra("CategoryId", categoryId);

                        //Start New Activity
                        viewHolder.food_image.setTransitionName("thumbnailTransition");
                        Pair<View, String> pair1 = Pair.create((View) viewHolder.food_image, viewHolder.food_image.getTransitionName());
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(FoodList.this, pair1);
                        FoodList.this.startActivity(foodDetail, optionsCompat.toBundle());
                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        // Set Adapter
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
        if (searchAdapter != null)
            searchAdapter.stopListening();
    }
    public void setCartStatus(){
        priceTag = findViewById(R.id.checkout_layout_price);
        itemsCount = findViewById(R.id.items_count);
        int totalCount = 0;
        if (Common.currentUser != null)
            totalCount = new Database(this).getCountCart(Common.currentUser.getPhone());
//        else if (Paper.book().read("userPhone") != null)
//            totalCount =new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//
        else {
            startActivity(new Intent(FoodList.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
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

        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setCartStatus();
    }



}
