package com.spiroskafk.parking.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.activities.authentication.EmailSignInActivity;
import com.spiroskafk.parking.activities.authentication.SignInActivity;
import com.spiroskafk.parking.activities.company.CompanyActivity;
import com.spiroskafk.parking.activities.experimental.CompanyDashboard;
import com.spiroskafk.parking.activities.user.UserActivity;
import com.spiroskafk.parking.model.User;

public class SplashActivity extends AppCompatActivity {
    protected int _splashTime = 2000;

    private static final String TAG = SplashActivity.class.getSimpleName();

    // UI elements
    private Button loginButton;
    private Button signupButton;

    // Firebase vars
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                isSignedIn();
            }
        });

        // Check if user is already signed in


//        int secondsDelayed = 1;
//        new Handler().postDelayed(new Runnable()
//        {
//            public void run()
//            {
//                startActivity(new Intent(SplashActivity.this,
//                        UserActivity.class));
//                finish();
//            }
//        }, secondsDelayed * 1000);
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Log.i(TAG, "mAuth: " + mAuth.getCurrentUser());
        loginButton = findViewById(R.id.btn_login);
        signupButton = findViewById(R.id.btn_get_started);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                //startActivity(new Intent(SplashActivity.this, EmailSignInActivity.class));
            }
        });


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, SignupActivity.class));
                ;
            }
        });
    }

    private void isSignedIn() {
        Log.i(TAG, "isSignedIn");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User currentUser = dataSnapshot.getValue(User.class);
                            Log.i(TAG, "HI");
                            if (currentUser != null) {
                                Log.i(TAG, "User is : " + currentUser.getName());
                                String userType = currentUser.getType();
                                if (userType != null) {
                                    if (userType.equals("user")) {
                                        //startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                        startActivity(new Intent(SplashActivity.this, UserActivity.class));
                                    } else if (userType.equals("company")) {
                                        startActivity(new Intent(SplashActivity.this, CompanyDashboard.class));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Attach the authStatelistener
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detatch the authStateListener
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

}
