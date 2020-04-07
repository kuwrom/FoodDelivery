package com.habeshastudio.fooddelivery.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;

public class FlavoursViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textFlavourName, textFlavourPrice, textFlavourDescription;
    public LinearLayout vegetarianHolder;

    private ItemClickListener itemClickListener;

    public FlavoursViewHolder(@NonNull View itemView) {
        super(itemView);

        textFlavourName = itemView.findViewById(R.id.flavour_name);
        textFlavourDescription = itemView.findViewById(R.id.flavour_description);
        textFlavourPrice = itemView.findViewById(R.id.flavour_price);
        vegetarianHolder = itemView.findViewById(R.id.is_vegetarian_layout);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(), false);

    }
}
