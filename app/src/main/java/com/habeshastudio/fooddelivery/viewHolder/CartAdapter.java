package com.habeshastudio.fooddelivery.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.activities.Cart;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.models.Order;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kibrom on 14/04/2019.
 */


public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order> listData = new ArrayList<>();
    //private Context context;
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CartViewHolder holder, final int position) {

        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .resize(100, 100)
                .centerCrop()
                .into(holder.cart_image);

        holder.btn_quantity.setNumber(listData.get(position).getQuantity());
        final Animation animShake = AnimationUtils.loadAnimation(cart, R.anim.swipe_left);
        holder.cartDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.view_foreground.startAnimation(animShake);
                //Toast.makeText(cart, "Swipe from left to delete this", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {

                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update total
                int total = 0;
                List<Order> orders = new Database(cart).getCarts(Common.currentUser.getPhone());
                for (Order item : orders)
                    total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(item.getQuantity()));
                Locale locale = new Locale("en", "US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                double amount;
                String formatAmount = cart.txtTotalPrice.getText().toString()
                        .replace("$", "")
                        .replace(",", "");
                String formatAmountETB = cart.txtTotalPrice.getText().toString()
                        .replace("ETB", "")
                        .replace(" ", "");
                double subTotal;
                if (Common.isUsdSelected) {
                    cart.txtTotalPrice.setText(fmt.format((total + cart.currentDeliveryPrice) / Common.ETB_RATE));
                    amount = Float.parseFloat(formatAmount);
                    subTotal = (amount-(cart.currentDeliveryPrice/Common.ETB_RATE))/1.15;
                    //update other texts
                    double tax = subTotal*0.15;
                    cart.subTotalView.setText(fmt.format(subTotal));
                    cart.taxView.setText(fmt.format(tax));
                }else {
                    cart.txtTotalPrice.setText(String.format("ETB %s", total + cart.currentDeliveryPrice));
                    amount = Float.parseFloat(formatAmountETB);
                    subTotal = (amount-cart.currentDeliveryPrice)/1.15;
                    //update other texts
                    double tax = subTotal*0.15;
                    cart.subTotalView.setText(String.format("ETB %s",subTotal));
                    cart.taxView.setText(String.format("ETB %s",tax));
                }
                cart.calculateTotalPrice();

            }
        });

        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice())) * (Integer.parseInt(listData.get(position).getQuantity()));
        //holder.txt_price.setText(fmt.format(price));
        if (Common.isUsdSelected)
            holder.txt_price.setText(fmt.format(price/Common.ETB_RATE));
        else holder.txt_price.setText(String.format("ETB %s", price));
            holder.txt_cart_name.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position) {
        return listData.get(position);
    }

    public void removeItem(int position) {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position) {
        listData.add(position, item);
        notifyItemInserted(position);
    }
}
