package com.habeshastudio.fooddelivery.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.models.Request;
import com.habeshastudio.fooddelivery.models.User;
import com.habeshastudio.fooddelivery.viewHolder.OrderViewHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class OrderHistory extends AppCompatActivity {

    public EmptyRecyclerView recyclerView;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference requests;
    DatabaseReference users;
    LinearLayout checkoutButton;
    TextView itemsCount, priceTag;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout refreshOrders;

    @Override
    protected void attachBaseContext(Context newBase) {
        //super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        super.attachBaseContext(LocaleHelper.onAtach(newBase, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rf.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_order_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "Order History");
        toolbar.setNavigationIcon((int) R.drawable.ic_baseline_arrow_back_ios_24);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                OrderHistory.this.finish();
            }
        });
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        Paper.init(OrderHistory.this);
        //Init Firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        requests = database.getReference("ForTheRecord");
        refreshOrders = findViewById(R.id.refresh_orders);
        //final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        checkoutButton = findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(OrderHistory.this, Cart.class);
                startActivity(cartIntent);
            }
        });

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


        refreshOrders.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        refreshOrders.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshOrders.setRefreshing(false);
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadOrders(Common.currentUser.getPhone());
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        //load for first time
        refreshOrders.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadOrders(Paper.book().read("userPhone").toString());
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    refreshOrders.setRefreshing(false);
                    return;
                }
            }
        });


        try {
            if (getIntent() == null)
                loadOrders(Paper.book().read("userPhone").toString());
            else {
                if (getIntent().getStringExtra("userPhone") == null)
                    loadOrders(Paper.book().read("userPhone").toString());
                else
                    loadOrders(getIntent().getStringExtra("userPhone"));
            }
        } catch (Exception E) {
            startActivity(new Intent(OrderHistory.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }


        setCartStatus();
        final BubbleNavigationLinearView bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {
                    startActivity(new Intent(OrderHistory.this, Home.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 1) {
                    startActivity(new Intent(OrderHistory.this, OrderStatus.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 2) {
                    startActivity(new Intent(OrderHistory.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 3) {
                    startActivity(new Intent(OrderHistory.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 4) {

                }
            }
        });
    }


    private void loadOrders(String phone) {
        try {
            Query getOrderByUser = requests.orderByChild("phone").equalTo(phone);
            FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(getOrderByUser, Request.class)
                    .build();
            getOrderByUser.keepSynced(true);
            adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {

                @Override
                protected void onBindViewHolder(@NonNull final OrderViewHolder viewHolder, @SuppressLint("RecyclerView") final int position, @NonNull final Request model) {

                    viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                    viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                    viewHolder.txtOrderAddress.setText(model.getAddress());
                    viewHolder.txtOrderphone.setText(model.getPhone());
                    viewHolder.btn_delete.setVisibility(View.GONE);
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Common.currentKey = adapter.getRef(position).getKey();
                            //
                            // startActivity(new Intent(OrderStatus.this, TrackingOrder.class));

                            Intent orderDetail = new Intent(OrderHistory.this, OrderHistoryDetail.class);
                            Common.currentRequest = model;
                            orderDetail.putExtra("OrderId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                            startActivity(orderDetail);
                        }
                    });

                }

                @NonNull
                @Override
                public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.order_layout, parent, false);
                    return new OrderViewHolder(itemView);
                }
            };
            adapter.startListening();
            recyclerView.setAdapter(adapter);
            recyclerView.setEmptyView(findViewById(R.id.empty_view_orders));
        } catch (Exception e) {
            Log.e("error name", e.getMessage());
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Common.isConnectedToInternet(getBaseContext())) {
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
            loadOrders(Paper.book().read("userPhone").toString());
        } else {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }
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
            startActivity(new Intent(OrderHistory.this, MainActivity.class));
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
    protected void onStart() {
        super.onStart();
        setCartStatus();
    }
}
