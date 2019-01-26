package com.spiroskafk.parking.activities.user;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.User;

import java.util.HashMap;

public class RewardsActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = RewardsActivity.class.getSimpleName();

    // UI Components
    private CardView mReward1Cardview;
    private CardView mReward2Cardview;
    private CardView mReward3Cardview;
    private CardView mReward4Cardview;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mFirebaseRef;
    private FirebaseDatabase mFirebaseDatabase;

    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        init();

        setupListeners();
    }

    private void init() {
        // Init - Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mReward1Cardview = findViewById(R.id.reward_1_cardview);
        mReward2Cardview = findViewById(R.id.reward_2_cardview);
        mReward3Cardview = findViewById(R.id.reward_3_cardview);
        mReward4Cardview = findViewById(R.id.reward_4_cardview);

        // Get current user
        userId = mAuth.getInstance().getCurrentUser().getUid();

        // Init Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDatabase.getReference().child("users").child(userId);
    }

    private void setupListeners() {

        mReward1Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 50
                updateUserProfile(50);

            }
        });

        mReward2Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 150
                updateUserProfile(150);


            }
        });

        mReward3Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 300
                updateUserProfile(300);


            }
        });

        mReward4Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 500
                updateUserProfile(500);


            }
        });

    }

    private void updateUserProfile(final int points) {
        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get user data
                User user = dataSnapshot.getValue(User.class);

                // Check if user exists
                if (user == null) return;

                // Check if user has enough points
                if (user.getRewardPoints() >= points) {
                    // Update his points
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("rewardPoints", user.getRewardPoints() - points);
                    mFirebaseRef.updateChildren(data);
                    Toast.makeText(RewardsActivity.this, "Purchased successfully!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(RewardsActivity.this, "You don't have enough points to buy this reward!", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
