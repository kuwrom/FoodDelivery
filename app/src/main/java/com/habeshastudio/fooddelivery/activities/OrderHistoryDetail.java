package com.habeshastudio.fooddelivery.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.helper.MyExceptionHandler;
import com.habeshastudio.fooddelivery.viewHolder.OrderDetailAdapter;

import java.util.HashMap;

public class OrderHistoryDetail extends AppCompatActivity {

    TextView order_id, order_phone, order_address, order_total, order_comment;
    String order_id_value;
    RecyclerView listFoods;
    Button reOrder;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_detail);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "Order Details");
        toolbar.setNavigationIcon((int) R.drawable.ic_baseline_arrow_back_ios_24);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                OrderHistoryDetail.this.finish();
            }
        });
        setSupportActionBar(toolbar);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        if (getIntent() != null)
            order_id_value = getIntent().getStringExtra("OrderId");

        order_id = (TextView) findViewById(R.id.order_id);
        order_phone = (TextView) findViewById(R.id.order_phone);
        order_address = (TextView) findViewById(R.id.order_addres);
        order_total = (TextView) findViewById(R.id.order_total);
        order_comment = (TextView) findViewById(R.id.order_comment);
        reOrder = findViewById(R.id.re_order);
        //callRestaurant = findViewById(R.id.btn_call_restaurant);
        listFoods = (RecyclerView) findViewById(R.id.lstFoods);
        listFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listFoods.setLayoutManager(layoutManager);


        reOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderHistoryDetail.this);
                alertDialog.setTitle("This will cost you " + Common.currentRequest.getTotal().split(" ")[1] + " birr");
                alertDialog.setMessage("Are you sure, do you want to continue?\n\ni.e. Shipping to your last Location");
                alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String orderId = String.valueOf(System.currentTimeMillis());
                        if (order_id_value != null && !order_id_value.isEmpty())
                            FirebaseDatabase.getInstance().getReference("ForTheRecord").child(order_id_value)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final HashMap<String, Object> reOrdered = new HashMap<>();
                                            for (DataSnapshot newOrder : dataSnapshot.getChildren()) {
                                                reOrdered.put(newOrder.getKey(), newOrder.getValue());
                                            }
                                            reOrdered.put("status", "0");
                                            reOrdered.put("paymentState", "Unpaid");
                                            reOrdered.put("paymentMethod", "COD");

                                            FirebaseDatabase.getInstance().getReference("Requests").child(orderId).updateChildren(reOrdered)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            FirebaseDatabase.getInstance().getReference("ForTheRecord").child(orderId).setValue(reOrdered)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            startActivity(new Intent(OrderHistoryDetail.this, OrderStatus.class));
                                                                            finish();
                                                                        }
                                                                    });
                                                        }
                                                        });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                        else
                            Toast.makeText(OrderHistoryDetail.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });
        //set Values
        order_id.setText("ID: " + order_id_value);
        order_phone.setText("Order From: " + Common.currentRequest.getName());
        order_total.setText("Total Price: " + Common.currentRequest.getTotal());
        order_address.setText("Home Address: " + Common.currentRequest.getAddress());
        order_comment.setText("Order Comments:\n" + Common.currentRequest.getComment());

        OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        listFoods.setAdapter(adapter);

    }

}
