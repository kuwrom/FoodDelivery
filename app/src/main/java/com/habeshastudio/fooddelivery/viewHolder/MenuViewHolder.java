package com.habeshastudio.fooddelivery.viewHolder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;


/**
 * Created by Kibrom on 2019/11/17.
 */

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtMenuName;
    public TextView restaurantTime;
    public ImageView imageView;
    public CardView restaurantHolder;
    TextView freeDelivery;
    ImageView isOpened;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(View itemView) {
        super(itemView);

        txtMenuName = itemView.findViewById(R.id.menu_name);
        freeDelivery = itemView.findViewById(R.id.free_delivery_tag);
        imageView = itemView.findViewById(R.id.menu_image);
        isOpened = itemView.findViewById(R.id.online_dot);
        restaurantTime = itemView.findViewById(R.id.time_to_restaurant);
        restaurantHolder = itemView.findViewById(R.id.restaurant_holder);


        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
