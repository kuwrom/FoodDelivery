package com.habeshastudio.fooddelivery.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.habeshastudio.fooddelivery.helper.EmptyRecyclerView;
import com.habeshastudio.fooddelivery.helper.LocaleHelper;
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

public class OrderStatus extends AppCompatActivity {

    public EmptyRecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference requests, history;
    DatabaseReference users;
    LinearLayout checkoutButton;
    TextView itemsCount, priceTag;
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
        setContentView(R.layout.activity_order_status);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.orders));
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        Paper.init(OrderStatus.this);
        //Init Firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        refreshOrders = findViewById(R.id.refresh_orders);
        requests = database.getReference("Requests");
        history = database.getReference("ForTheRecord");
        //final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        checkoutButton =findViewById(R.id.btn_checkout_cart);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent = new Intent(OrderStatus.this, Cart.class);
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


        //load menu for first time
        refreshOrders.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadOrders(Common.currentUser.getPhone());
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    refreshOrders.setRefreshing(false);
                    return;
                }
            }
        });


        setCartStatus();
        final BubbleNavigationLinearView bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/rf.ttf"));
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (position == 0) {
                    startActivity(new Intent(OrderStatus.this, Home.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    finish();
                } else if (position == 2) {
                    startActivity(new Intent(OrderStatus.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 3) {
                    startActivity(new Intent(OrderStatus.this, FavoritesActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (position == 4) {
                    startActivity(new Intent(OrderStatus.this, Profile.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
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

            adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {

                @Override
                protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, @SuppressLint("RecyclerView") final int position, @NonNull Request model) {

                    viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                    viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                    viewHolder.txtOrderAddress.setText(model.getAddress());
                    viewHolder.txtOrderphone.setText(model.getPhone());
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Common.currentKey = adapter.getRef(position).getKey();
                            startActivity(new Intent(OrderStatus.this, TrackingOrder.class));
                        }
                    });

                    viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
                            alertDialog.setTitle(getResources().getString(R.string.delete_order));
                            alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (adapter.getItem(position).getStatus().equals("0"))
                                        deleteOrder(adapter.getRef(position).getKey());
                                    else
                                        Toast.makeText(OrderStatus.this, getResources().getString(R.string.cant_delete_order), Toast.LENGTH_SHORT).show();

                                }
                            });
                            alertDialog.show();

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
        } catch(Exception e) {
            Log.e("error name", e.getMessage());
        }

    }

    private void deleteOrder(final String key) {

        history.child(key).child("status").setValue("4").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

        requests.child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        double amount = 0.0;
//                        try{
//                            amount = Common.formatCurrency( dataSnapshot.child("total").getValue().toString(), Locale.US).doubleValue();
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            return;
//                        }

                        //refund
                        //double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) + amount;
                        //Map<String, Object> update_balance = new HashMap<>();
                        //update_balance.put("balance", balance);
//                        FirebaseDatabase.getInstance().getReference("User")
//                                .child(Common.currentUser.getPhone())
//                                .updateChildren(update_balance)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
                                            requests.child(key)
                                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //Toast.makeText(OrderStatus.this, new StringBuilder("Order ").append(key).append(" has been deleted!").toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(OrderStatus.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            //Refresh user status
                                            FirebaseDatabase.getInstance().getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
//                                    }
//                                });
//                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
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

    public void setCartStatus() {

        priceTag = findViewById(R.id.checkout_layout_price);
        itemsCount = findViewById(R.id.items_count);
        int totalCount = 0;
        if (Common.currentUser != null)
            totalCount = new Database(this).getCountCart(Common.currentUser.getPhone());
//        else if (Paper.book().read("userPhone") != null)
//            totalCount =new Database(this).getCountCart(Paper.book().read("userPhone").toString());
//
        else {
            startActivity(new Intent(OrderStatus.this, MainActivity.class));
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
            //priceTag.setText(fmt.format(total));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setCartStatus();
    }
}
