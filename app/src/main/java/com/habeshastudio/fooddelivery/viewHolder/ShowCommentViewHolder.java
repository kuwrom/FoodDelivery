package com.habeshastudio.fooddelivery.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.habeshastudio.fooddelivery.R;


public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtUserPhone, txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(@NonNull View itemView) {
        super(itemView);
        txtComment = itemView.findViewById(R.id.commentTextComment);
        txtUserPhone = itemView.findViewById(R.id.commentUserPhone);
        ratingBar = itemView.findViewById(R.id.commentRatingBar);


    }
}
