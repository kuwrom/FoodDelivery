package com.habeshastudio.fooddelivery.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;


/**
 * Created by Kibrom on 2019/11/20.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId, txtOrderStatus, txtOrderBy, txtOrderedFrom, txtAssignedHandler, txtTotalPrice, txtPaymentState;
    public ImageView btn_delete;
    private ItemClickListener itemClickListener;


    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderBy = itemView.findViewById(R.id.order_by);
        txtOrderedFrom = itemView.findViewById(R.id.order_from);
        txtAssignedHandler = itemView.findViewById(R.id.order_handler);
        txtTotalPrice = itemView.findViewById(R.id.total_price);
        txtPaymentState = itemView.findViewById(R.id.payment_state);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        btn_delete = itemView.findViewById(R.id.btn_delete);

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
