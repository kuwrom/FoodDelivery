package com.habeshastudio.fooddelivery.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.MainActivity;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.helper.EmptyRecyclerView;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.helper.RecyclerItemTouchHelper;
import com.habeshastudio.fooddelivery.interfaces.RecyclerItemTouchHelperListener;
import com.habeshastudio.fooddelivery.models.Favorites;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.User;
import com.habeshastudio.fooddelivery.viewHolder.FavoritesAdapter;
import com.habeshastudio.fooddelivery.viewHolder.FavoritesViewHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    EmptyRecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    DatabaseReference users;

    LinearLayout checkoutButton;

    TextView itemsCount, priceTag;

    FavoritesAdapter adapter;
    RelativeLayout rootLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rf.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_favorites);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.favorites));
        setSupportActionBar(toolbar);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        Paper.init(FavoritesActivity.this);

        users = FirebaseDatabase.getInstance().getReference("User");

        rootLayout = findViewById(R.id.root_fav_layout);
        recyclerView = findViewById(R.id.recycler_fav);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        priceTag = findViewById(R.id.checkout_layout_price);
        itemsCount = findViewById(R.id.items_count);
        recyclerView.setLayoutManager(layoutManager);


        checkoutButton =findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(FavoritesActivity.this, Cart.class);
                startActivity(cartIntent);
            }
        });

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);
        loadFavorites();

        final BubbleNavigationLinearView bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {
                    startActivity(new Intent(FavoritesActivity.this, Home.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 1) {
                    startActivity(new Intent(FavoritesActivity.this, OrderStatus.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 2) {

                } else if (position == 3) {


                } else if (position == 4) {
                    startActivity(new Intent(FavoritesActivity.this, Profile.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {

                }
            }
        });
    }

    private void loadFavorites() {
        if (Common.currentUser != null)
            adapter = new FavoritesAdapter(this, new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        else {
            startActivity(new Intent(FavoritesActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(findViewById(R.id.empty_view_fav));
        setCartStatus();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder) {
            String name = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(position).getFoodName();
            final int deleteIndex = viewHolder.getAdapterPosition();
            final Favorites deleteItem = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(deleteIndex);
            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromFavourites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            //make snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " " + getResources().getString(R.string.removed_from_fav), Snackbar.LENGTH_LONG);
            snackbar.setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToFavourites(deleteItem);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            setCartStatus();
        }
    }

    public void setCartStatus() {

        int totalCount = 0;
        if (Common.currentUser != null)
            totalCount = new Database(this).getCountCart(Common.currentUser.getPhone());
//        else if (Paper.book().read("userPhone") != null)
//            totalCount =new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//
        else {
            startActivity(new Intent(FavoritesActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
        if (totalCount == 0)
            checkoutButton.setVisibility(View.GONE);
        else {
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
    protected void onPostResume() {
        super.onPostResume();
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

    @Override
    protected void onStart() {
        super.onStart();
        setCartStatus();
    }
}
