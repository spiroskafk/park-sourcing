package com.spiroskafk.parking;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class UserActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    // UI components
    private Button reportBtn;
    private Button rentBtn;
    private Button findBtn;
    private Button rewardBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize UI components
        initUIComponents();

        // Initialize Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Check if the user is signed in or not
                // firebaseAuth contains that information
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    // signed in

                } else {
                    // signed out
                    // Here we want to launch the sign in flow
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };

        // Set up button listeners
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send us to report position activity
                Intent launchReportActivity = new Intent(UserActivity.this, ReportPositionActivity.class);
                startActivity(launchReportActivity);
            }
        });

        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send us to google maps activity
                Intent launchMapsActivity = new Intent(UserActivity.this, MapsActivity.class);
                startActivity(launchMapsActivity);
            }
        });
    }

    private void initUIComponents() {
        reportBtn = findViewById(R.id.report_btn);
        rentBtn = findViewById(R.id.rent_btn);
        findBtn = findViewById(R.id.find_btn);
        rewardBtn = findViewById(R.id.reward_btn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Attach the authStatelistener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detatch the authStateListener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                // sign out
                FirebaseAuth.getInstance().signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
