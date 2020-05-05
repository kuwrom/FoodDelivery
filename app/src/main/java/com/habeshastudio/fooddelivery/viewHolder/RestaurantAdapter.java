package com.habeshastudio.fooddelivery.viewHolder;

import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.activities.FoodList;
import com.habeshastudio.fooddelivery.activities.Home;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.interfaces.ItemClickListener;
import com.habeshastudio.fooddelivery.models.Category;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kibrom on 14/04/2019.
 */


public class RestaurantAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    private HashMap<String, Category> restaurantList;
    private List<String> categoryKeyList = new ArrayList<>();


    //private Context context;
    private Home home;

    public RestaurantAdapter(HashMap<String, Category> restaurantList, Home home) {
        this.restaurantList = restaurantList;
        for (Map.Entry<String, Category > pair: restaurantList.entrySet()) {
                categoryKeyList.add(pair.getKey());
            }
        this.home = home;
    }

    @Override
    public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(home);
        View itemView = inflater.inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MenuViewHolder viewHolder, int position) {

        viewHolder.txtMenuName.setText(restaurantList.get(categoryKeyList.get(position)).getName());
        Location temp = new Location("A");
        String[] restaurantLatLng = restaurantList.get(categoryKeyList.get(position)).getLocation().split(",");
        temp.setLatitude(Double.parseDouble(restaurantLatLng[0]));
        temp.setLongitude(Double.parseDouble(restaurantLatLng[1]));
        double distance = Common.currentUserLocation.distanceTo(temp);
        Common.restaurantDistance.put(categoryKeyList.get(position), distance);
        //Toast.makeText(home, String.valueOf(categoryKeyList.get(position)), Toast.LENGTH_SHORT).show();
        viewHolder.restaurantTime.setText(String.format("%.0f",20+(distance/165))+" minutes away");

        Picasso.with(home.getBaseContext()).load(restaurantList.get(categoryKeyList.get(position)).getImage()).placeholder(R.drawable.restaurantbg)
                .into(viewHolder.imageView);

        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClik) {
                //Get CategoryId and Send to new activity
                Intent intent = new Intent(home, FoodList.class);
                intent.putExtra("CategoryId", categoryKeyList.get(position));
                home.startActivity(intent);
                home.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }
}
