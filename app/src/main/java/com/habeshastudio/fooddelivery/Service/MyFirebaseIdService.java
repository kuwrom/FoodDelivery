package com.habeshastudio.fooddelivery.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.habeshastudio.fooddelivery.common.Common;
import com.habeshastudio.fooddelivery.Model.Token;

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
            tokens.child(Common.currentUser.getPhone()).setValue(token);
        }
    }
}
