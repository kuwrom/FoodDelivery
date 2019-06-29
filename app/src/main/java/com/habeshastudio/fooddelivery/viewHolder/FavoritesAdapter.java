package com.habeshastudio.fooddelivery.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.database.Database;
import com.habeshastudio.fooddelivery.activities.FoodDetail;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;
import com.habeshastudio.fooddelivery.models.Favorites;
import com.habeshastudio.fooddelivery.models.Order;
import com.habeshastudio.fooddelivery.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    private Context context;
    private List<Favorites> favoritesList;

    public FavoritesAdapter(Context context, List<Favorites> favorites) {
        this.context = context;
        this.favoritesList = favorites;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.favorites_item, viewGroup, false);
        return new FavoritesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder viewHolder, final int position) {
        viewHolder.food_name.setText(favoritesList.get(position).getFoodName());
        viewHolder.food_price.setText(String.format("$ %s", favoritesList.get(position).getFoodPrice()));
        Picasso.with(context).load(favoritesList.get(position).getFoodImage())
                .into(viewHolder.food_image);

        //quick cart
        final boolean isAddedToCart = new Database(context).isAddedToCart(favoritesList.get(position).getFoodId(), Common.currentUser.getPhone());

        viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAddedToCart) {
                    new Database(context).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            favoritesList.get(position).getFoodId(),
                            favoritesList.get(position).getFoodName(),
                            "1",
                            favoritesList.get(position).getFoodPrice(),
                            favoritesList.get(position).getFoodDiscount(),
                            favoritesList.get(position).getFoodImage()

                    ));

                    Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Food already added to cart", Toast.LENGTH_SHORT).show();
                }
            }
        });


        final Favorites local = favoritesList.get(position);
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClik) {
                //Start New Activity
                Intent foodDetail = new Intent(context, FoodDetail.class);
                foodDetail.putExtra("FoodId", favoritesList.get(position).getFoodId()); //Send food Id to new activity
                context.startActivity(foodDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public void removeItem(int position) {
        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item, int position) {
        favoritesList.add(position, item);
        notifyItemInserted(position);
    }

    public Favorites getItem(int position) {
        return favoritesList.get(position);
    }
}
