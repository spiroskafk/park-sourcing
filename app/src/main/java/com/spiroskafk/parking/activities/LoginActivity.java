package com.spiroskafk.parking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.activities.experimental.CompanyDashboard;
import com.spiroskafk.parking.activities.user.UserActivity;

public class LoginActivity extends AppCompatActivity {

    // Log tag
    private static final String TAG = LoginActivity.class.getSimpleName();

    // Firebase vars
    private FirebaseAuth mAuth;
    private String userType;

    // UI elements
    private ProgressBar mProgress;
    private EditText mEmailAddress;
    private EditText mPassword;
    private Button mLoginButton;
    private TextView mSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        buttonListeners();
    }

    private void init() {
        mEmailAddress = findViewById(R.id.et_email_address);
        mPassword = findViewById(R.id.et_password);
        mLoginButton = findViewById(R.id.btn_login);
        mProgress = findViewById(R.id.progressBar);
        mSignup = findViewById(R.id.sign_up);
        mAuth = FirebaseAuth.getInstance();

    }

    private void buttonListeners() {
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        final String email = mEmailAddress.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            mProgress.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // We need to determine what type of user is trying to login
                    String userId = mAuth.getCurrentUser().getUid();
                    Log.i(TAG, "userID: " + userId);

                    // Fetch data ONCE
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userType = dataSnapshot.child("type").getValue(String.class);

                            // NPE check
                            if (userType != null) {
                                switch (userType) {
                                    case "user":
                                        mProgress.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(LoginActivity.this, UserActivity.class));
                                        finish();
                                        break;

                                    case "company":
                                        mProgress.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(LoginActivity.this, CompanyDashboard.class));
                                        finish();
                                        break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
    }

}
