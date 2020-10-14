package com.habeshastudio.fooddelivery.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.viewHolder.OrderDetailAdapter;

public class OrderDetail extends AppCompatActivity {

    TextView order_id, order_phone, order_address, order_total, order_comment;
    String order_id_value;
    RecyclerView listFoods;
    Button processed;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Order Details");
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                OrderDetail.this.finish();
            }
        });
        setSupportActionBar(toolbar);

        order_id = findViewById(R.id.order_id);
        order_phone = findViewById(R.id.order_phone);
        order_address = findViewById(R.id.order_addres);
        order_total = findViewById(R.id.order_total);
        order_comment = findViewById(R.id.order_comment);
        processed = findViewById(R.id.order_processed);
        //callRestaurant = findViewById(R.id.btn_call_restaurant);
        listFoods = findViewById(R.id.lstFoods);
        listFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listFoods.setLayoutManager(layoutManager);


        if (getIntent() != null)
            order_id_value = getIntent().getStringExtra("OrderId");
        processed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
