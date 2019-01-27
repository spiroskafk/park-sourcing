package com.spiroskafk.parking.activities.company;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.adapters.UserAdapter;
import com.spiroskafk.parking.model.User;

import java.util.ArrayList;
import java.util.ListIterator;

public class ParkedUsersActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = ParkedUsersActivity.class.getSimpleName();

    // ID of the specific PrivateParking company
    private String houseId;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    // Parked users
    private ArrayList<User> userList;

    // Application context
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parked_users);

        init();

        // Read data from database
        readFromDatabase();

    }

    private void init() {

        // Init toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init context
        context = this;

        // Get data from CompanyDashboard
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            houseId = extras.getString("houseId");
        }

        // Init recyclerview
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Init userList - contains parked users
        userList = new ArrayList<>();
    }

    /**
     * Setups firebase database listeners in order to read data
     */
    private void readFromDatabase() {

        // Callback in order to get all available users
        // Saves in userList only the users that are parked in this specific PrivateParking
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
                            userList.add(currentUser);
                            adapter = new UserAdapter(context, userList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            if (contains(userList, currentUser)) {
                                if (!userList.isEmpty()) {

                                    ListIterator<User> iter = userList.listIterator();
                                    while (iter.hasNext()) {
                                        if (iter.next().getEmail().equals(currentUser.getEmail())) {
                                            iter.remove();
                                        }
                                    }
                                }
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

    /**
     * Checks if user exists on the list
     * @param list
     * @param user
     * @return true if user found in list
     */
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
