package com.habeshastudio.fooddelivery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.Interface.ItemClickListener;
import com.habeshastudio.fooddelivery.Model.Banner;
import com.habeshastudio.fooddelivery.Model.Category;
import com.habeshastudio.fooddelivery.Model.Token;
import com.habeshastudio.fooddelivery.ViewHolder.MenuViewHolder;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference category;
    DatabaseReference users;
    //CounterFab fab;

    TextView txtFullName, menu_name;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    //Slider
    HashMap<String, String> image_list;
    SliderLayout mSlider;

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
        setContentView(R.layout.activity_home);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }


        //Init Firebase
        database = FirebaseDatabase.getInstance();
        users = FirebaseDatabase.getInstance().getReference("User");
        category = database.getReference("Category");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Common.currentUser.setPhone(user.getPhoneNumber());

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();

        final BubbleNavigationLinearView bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {
                    startActivity(new Intent(Home.this, Profile.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 1) {
                    startActivity(new Intent(Home.this, OrderStatus.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 2) {
                    startActivity(new Intent(Home.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 3) {
                    startActivity(new Intent(Home.this, SearchActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (position == 4) {

                } else {

                }
            }
        });
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Get CategoryId and Send to new activity
                        Intent intent = new Intent(Home.this, FoodList.class);
                        intent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });


            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(itemView);
            }
        };

        swipeRefreshLayout = findViewById(R.id.homeSwipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please Check your connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        //load menu for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please Check your connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        /*
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));


        */

        //Load Menu
        recycler_menu = findViewById(R.id.recycler_menu);
        //recycler_menu.setHasFixedSize(true);
        //layoutManager = new LinearLayoutManager(this);
        //recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);


        updateToken(FirebaseInstanceId.getInstance().getToken());

        //setup Slider
        setupSlider();
    }

    private void setupSlider() {
        mSlider = findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Banner banner = postSnapshot.getValue(Banner.class);
                    image_list.put(banner.getName() + "@@@" + banner.getId(), banner.getImage());
                }
                for (String key : image_list.keySet()) {
                    String[] keySplit = key.split("@@@");
                    String nameOffood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //crete slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOffood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, FoodDetail.class);
                                    //send food id to food detail
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });
                    //Add extra Bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId", idOfFood);

                    mSlider.addSlider(textSliderView);
                    //remove Event after finish
                    banners.removeEventListener(this);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.FlipHorizontal);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //////fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        if (adapter != null)
            adapter.startListening();
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    private void loadMenu() {

        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
