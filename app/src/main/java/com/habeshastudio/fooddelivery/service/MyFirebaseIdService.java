package com.habeshastudio.fooddelivery.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.models.Token;

import io.paperdb.Paper;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null)
            updateTokenToFirebase(tokenRefreshed);

    }

    private void updateTokenToFirebase(String tokenRefreshed) {

        if (Common.currentUser != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference("Tokens");
            Token token = new Token(tokenRefreshed, false);
            tokens.child(Paper.book().read("userPhone").toString()).setValue(token);
        }
    }
}
