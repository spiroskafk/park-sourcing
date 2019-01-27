package com.spiroskafk.parking.activities.company;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public class CompanyDashboard extends AppCompatActivity {

    // Log TAG
    private static final String TAG = CompanyDashboard.class.getSimpleName();

    // UI Components
    private CardView mStatistics;
    private CardView mGoogleMaps;
    private CardView mProfile;
    private CardView mSignOut;

    // Firebase components
    private FirebaseAuth mAuth;

    // HashMapp
    private HashMap<String, PrivateParking> privateHouses;

    // ParkingHouseId
    private String houseId;

    // Current company user
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize vars
        init();

        // Read data from database
        readFromDatabase();

        // Setup UI listeners
        registerUIListeners();
    }

    private void init() {
        mStatistics = findViewById(R.id.statistics);
        mGoogleMaps = findViewById(R.id.maps);
        mProfile = findViewById(R.id.profile);
        mSignOut = findViewById(R.id.sign_out);
        mAuth = FirebaseAuth.getInstance();
        privateHouses = new HashMap<String, PrivateParking>();
    }

    private void readFromDatabase() {
        // Get company info
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null)
                    user = currentUser;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Populates privateHouses<String, PrivateParking> HashMap
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("private_parking");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PrivateParking parking = dataSnapshot.getValue(PrivateParking.class);
                if (parking != null && user != null) {
                    if (parking.getEmail().equals(user.getEmail())) {
                        privateHouses.put(dataSnapshot.getKey(), parking);
                        houseId = dataSnapshot.getKey();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void registerUIListeners() {
        mStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && privateHouses != null & houseId != null) {
                    Intent i = new Intent(CompanyDashboard.this, InventoryActivity.class);
                    i.putExtra("houses", privateHouses);
                    i.putExtra("user", user);
                    i.putExtra("houseId", houseId);
                    startActivity(i);
                }
            }
        });

        mGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && privateHouses != null & houseId != null) {
                    Intent i = new Intent(CompanyDashboard.this, CompanyMaps.class);
                    i.putExtra("houses", privateHouses);
                    i.putExtra("user", user);
                    i.putExtra("houseId", houseId);
                    startActivity(i);
                }
            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (houseId != null) {
                    Intent i = new Intent(CompanyDashboard.this, ParkedUsersActivity.class);
                    i.putExtra("houseId", houseId);
                    startActivity(i);
                }
            }
        });

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });
    }

}
