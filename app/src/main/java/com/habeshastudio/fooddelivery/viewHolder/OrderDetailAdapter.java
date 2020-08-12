package com.habeshastudio.fooddelivery.viewHolder;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.models.Order;

import java.util.List;

class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView name, quantity, price, discount, from;

    public MyViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.product_name);
        quantity = (TextView) itemView.findViewById(R.id.product_quantity);
        price = (TextView) itemView.findViewById(R.id.product_price);
        discount = (TextView) itemView.findViewById(R.id.product_discount);
        from = itemView.findViewById(R.id.product_ordered_from);

    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_ayout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Order order = myOrders.get(position);
        holder.name.setText(String.format("Name : %s", order.getProductName()));
        holder.quantity.setText(String.format("Quantity : %s", order.getQuantity()));
        holder.price.setText(String.format("Price : %s", order.getPrice()));
        holder.discount.setText(String.format("Discount : %s", order.getDiscount()));

        final String[] str = order.getProductId().split("&");
        FirebaseDatabase.getInstance().getReference("Foods").child(str[0]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference("Category").
                        child(dataSnapshot.child("menuId").getValue().
                                toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.from.setText("Restaurant: " + dataSnapshot.child("name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
