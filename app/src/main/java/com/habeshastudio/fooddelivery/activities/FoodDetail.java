package com.habeshastudio.fooddelivery.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;
import com.habeshastudio.fooddelivery.models.Food;
import com.habeshastudio.fooddelivery.models.FoodMenu;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.Rating;
import com.habeshastudio.fooddelivery.models.User;
import com.habeshastudio.fooddelivery.viewHolder.FlavoursViewHolder;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_description, itemsCount, priceTag;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;

    RatingBar ratingBar;
    LinearLayout checkoutButton;
    String foodId = "";
    DatabaseReference users;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<FoodMenu, FlavoursViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference foods, ratingTbl, foodList;

    TextView btnShowComment;
    Food currentFood;

    FoodMenu currentFoodMenu;

    @Override
    protected void attachBaseContext(Context newBase) {
        //super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rf.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_food_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setTitle("Orders");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        Paper.init(FoodDetail.this);
        food_image = findViewById(R.id.img_food);

        getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transation));
        food_image.setTransitionName("thumbnailTransition");

        btnShowComment = findViewById(R.id.show_comment_button);
        checkoutButton =findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(FoodDetail.this, Cart.class);
                startActivity(cartIntent);
            }
        });


        //Firbase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        foods = database.getReference("Foods");
        foodList = foods.child(getIntent().getStringExtra("FoodId")).child("flavours");
        ratingTbl = database.getReference("Rating");

        Paper.init(this);

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

        FirebaseRecyclerOptions<FoodMenu> options = new FirebaseRecyclerOptions.Builder<FoodMenu>()
                .setQuery(foodList, FoodMenu.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<FoodMenu, FlavoursViewHolder>(options){

            @NonNull
            @Override
            public FlavoursViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.flavour_layout, viewGroup, false);
                return new FlavoursViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull FlavoursViewHolder holder, int position, @NonNull final FoodMenu model) {
                holder.textFlavourName.setText(model.getName());
                holder.textFlavourDescription.setText(model.getDescription());
                Locale locale = new Locale("en", "US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                int price = (Integer.parseInt(model.getPrice()));
                //holder.textFlavourPrice.setText(fmt.format(price));
                if (Common.isUsdSelected)
                    holder.textFlavourPrice.setText(fmt.format(price/Common.ETB_RATE));
                else holder.textFlavourPrice.setText(String.format("ETB %s", price));

                if (!model.isVegetarian()) holder.vegetarianHolder.setVisibility(View.GONE);
                if (model.getDescription().equals("") || model.getDescription() == null)
                    holder.textFlavourDescription.setVisibility(View.GONE);

                if (Common.isConnectedToInternet(getBaseContext())) {

                        holder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position, boolean isLongClick) {

                                if (Common.currentrestaurantID == null)Common.currentrestaurantID =Common.proposedrestaurantID;
                                if (Common.currentrestaurantID .equals(Common.proposedrestaurantID)) {
                                    Paper.book().write("restId", Common.currentrestaurantID);
                                    new Database(getBaseContext()).addToCart(new Order(
                                            Paper.book().read("userPhone").toString(),
                                            foodId + "&" + adapter.getRef(position).getKey(),
                                            currentFood.getName() + "\n" + model.getName(),
                                            "1",
                                            model.getPrice(),
                                            currentFood.getDiscount(),
                                            currentFood.getImage()
                                    ));
                                    setCartStatus();
                                }else{
                                    Toast.makeText(FoodDetail.this, getResources().getString(R.string.only_from_one), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();

        // Init view
        //btnCart = findViewById(R.id.btnCart);
        ratingBar = findViewById(R.id.ratingBar);

        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodDetail.this, ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID, foodId);
                startActivity(intent);
            }
        });

        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showRatingDialogue();
                return false;
            }
        });

        food_description = findViewById(R.id.food_description);
        food_name = findViewById(R.id.food_name);
        //food_price = findViewById(R.id.food_price);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Food Id From Internet
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()) {
            if (Common.isConnectedToInternet(getBaseContext())) {
                try {
                    getDetailFood(foodId);
                    getRatingFood(foodId);
                } catch (Exception e) {
                    onBackPressed();
                }



            } else {
                Toast.makeText(FoodDetail.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        }
        recyclerView = findViewById(R.id.details_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);
        loadMenu();
        setCartStatus();

//        //notification
//        Paper.init(FoodDetail.this);
//        if (Paper.book().read("restId") != null)
//            Common.currentrestaurantID = Paper.book().read("restId");
    }
    public void setCartStatus(){
        priceTag = findViewById(R.id.checkout_layout_price);
        itemsCount = findViewById(R.id.items_count);
        int totalCount = 0;
        if (Common.currentUser != null)
            totalCount = new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//        else if (Paper.book().read("userPhone") != null)
//            totalCount =new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//
        else {
            startActivity(new Intent(FoodDetail.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }

        if (totalCount == 0) {
            checkoutButton.setVisibility(View.GONE);
            Common.currentrestaurantID = null;
            Paper.book().delete("restId");
        }else{
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
            Paper.init(FoodDetail.this);
            if (Paper.book().read("beenToCart") != null)
                Common.alreadyBeenToCart = Paper.book().read("beenToCart");
            if (totalCount == 1 && !Common.alreadyBeenToCart){
                Common.alreadyBeenToCart = true;
                        Paper.book().write("beenToCart", true);
                Intent cartIntent = new Intent(FoodDetail.this, Cart.class);
                startActivity(cartIntent);
            }
            //priceTag.setText(fmt.format(total));
        }

    }

    private void loadMenu() {

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        //Animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialogue() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText(getResources().getString(R.string.submit))
                .setNegativeButtonText(getResources().getString(R.string.cancel))
                .setNoteDescriptions(Arrays.asList("Very Bad", "NOt Good", "Quite Ok", "Very Good", "Excellent"))
                .setDefaultRating(1)
                .setTitle(getResources().getString(R.string.rate_this))
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint(getResources().getString(R.string.comments_here))
                .setHintTextColor(R.color.grey_active)
                .setCommentTextColor(R.color.colorLightBlack)
                .setCommentBackgroundColor(R.color.grey_bg_light)
                .setWindowAnimation(R.style.RatingDialogueFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentFood.getImage())
                        .networkPolicy(NetworkPolicy.OFFLINE).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                //food_price.setText(currentFood.getPrice());

                food_name.setText(currentFood.getName());

                food_description.setText(currentFood.getDescription());

                loadMenu();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

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

            if (adapter == null){
                adapter.startListening();
                loadMenu();}
            else
            loadMenu();
        setCartStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setCartStatus();
        loadMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (adapter != null)
//        adapter.stopListening();
    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        final Rating rating = new Rating(Paper.book().read("userPhone").toString(), foodId, String.valueOf(value), comments);

        ratingTbl.push().setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, getResources().getString(R.string.thanks_for_rating), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
