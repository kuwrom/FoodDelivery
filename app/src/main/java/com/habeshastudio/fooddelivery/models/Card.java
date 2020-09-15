package com.habeshastudio.fooddelivery.models;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class Card {

    ArrayList<HashMap<String, Object>> list = new ArrayList<>();

    public Card(DataSnapshot snapshot) {


        // This is awesome! You don't have to know the data structure of the database.
        Object fieldsObj = new Object();

        HashMap cards;

        for (DataSnapshot shot : snapshot.getChildren()) {

            try {

                cards = (HashMap) shot.getValue(fieldsObj.getClass());

            } catch (Exception ex) {

                // My custom error handler. See 'ErrorHandler' in Gist
//                ErrorHandler.logError(ex);

                continue;
            }

            // Include the primary key of this Firebase data record. Named it 'recKeyID'
            cards.put("cards", shot.getKey());

            list.add(cards);
        }
    }

    public ArrayList<HashMap<String, Object>> getList() {
        return list;
    }

    public void setList(ArrayList<HashMap<String, Object>> list) {
        this.list = list;
    }
}
