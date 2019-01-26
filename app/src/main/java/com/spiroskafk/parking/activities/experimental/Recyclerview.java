package com.spiroskafk.parking.activities.experimental;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.adapters.UserAdapter;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Recyclerview extends AppCompatActivity {

    // Log TAG
    private static final String TAG = Recyclerview.class.getSimpleName();

    private String houseId;
    private User company;
    private HashMap<String, PrivateParking> privateHouses;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private ArrayList<User> userList;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        context = this;

        // Get data from CompanyDashboard
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            houseId = extras.getString("houseId");
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null && houseId != null) {
                    if (currentUser.getType().equals("user")) {
                        if (currentUser.getParkingHouseId().equals(houseId)) {
                            userList.add(currentUser);
                            adapter = new UserAdapter(context, userList);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null && houseId != null) {
                    if (currentUser.getType().equals("user")) {
                        if (currentUser.getParkingHouseId().equals(houseId)) {
                            Log.i(TAG, "changed in if");
                            userList.add(currentUser);
                            adapter = new UserAdapter(context, userList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.i(TAG, "changed in else");
                            Log.i(TAG, "USERLIST : " + userList.toString());
                            if (contains(userList, currentUser)) {
                                Log.i(TAG, "HEY");
                                if (!userList.isEmpty()) {
                                    for (User item : userList) {
                                        if (item.getEmail().equals(currentUser.getEmail()))
                                            userList.remove(item);
                                    }
                                }
                                Log.i(TAG, "listi nside: " + userList.toString());
                                adapter = new UserAdapter(context, userList);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null && houseId != null) {
                    if (currentUser.getType().equals("user")) {
                        if (userList.contains(currentUser)) {
                            userList.remove(currentUser);
                            adapter = new UserAdapter(context, userList);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean contains(ArrayList<User> list, User user) {
        for (User item : list) {
            if (item.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
