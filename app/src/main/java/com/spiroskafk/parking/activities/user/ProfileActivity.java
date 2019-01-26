package com.spiroskafk.parking.activities.user;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.User;

public class ProfileActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = ProfileActivity.class.getSimpleName();

    // UI components
    private TextView mDisplayName;
    private TextView mEmail;
    private TextView mReports;
    private TextView mPoints;
    private TextView mRating;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mFirebaseRef;
    private FirebaseDatabase mFirebaseDatabase;

    // UserId
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // Initialize phase
        init();

        // Update user Profile
        userId = mAuth.getInstance().getCurrentUser().getUid();
        if (userId != null) {
            updateProfile();
        }
    }

    private void init() {
        // Init - Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init UI
        mDisplayName = findViewById(R.id.text_display_name);
        mEmail = findViewById(R.id.text_email);
        mReports = findViewById(R.id.text_reports);
        mPoints = findViewById(R.id.text_points);
        mRating = findViewById(R.id.text_rating);

        // Init Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDatabase.getReference().child("users");

    }

    private void updateProfile() {
        mFirebaseRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Trustworthyness system
                if (user.getReports() < 10) {
                    mRating.setText("Haven't prove himself yet");
                } else if (user.getReports() < 20) {
                    mRating.setText("He has build quite a name!");
                } else if (user.getReports() < 30){
                    mRating.setText("Trustworthy");
                } else {
                    mRating.setText("Totally trusted user");
                }

                mDisplayName.setText(user.getName());
                mEmail.setText(user.getEmail().toString());
                mReports.setText(String.valueOf(user.getReports()));
                mPoints.setText(String.valueOf(user.getRewardPoints()));
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
