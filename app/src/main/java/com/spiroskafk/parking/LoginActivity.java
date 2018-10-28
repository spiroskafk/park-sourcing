package com.spiroskafk.parking;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginBtn;
    private Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);


//        mLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                userLogin();
//            }
//        });

    }

//    private void userLogin() {
//        final String email = mEmailEditText.getText().toString().trim();
//        final String password = mPasswordEditText.getText().toString().trim();
//
//        if (!email.isEmpty() && !password.isEmpty()) {
//            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if (task.isSuccessful()) {
//                        //Toast.makeText(LoginActivity.this, "You have successfully signed in!", Toast.LENGTH_SHORT).show();
//                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                        String registeredUserID = currentUser.getUid();
//                        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(registeredUserID);
//
//                        mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                String userType = dataSnapshot.child("type").getValue().toString();
//                                if (userType.equals("simple_user")) {
//                                    Toast.makeText(LoginActivity.this, "Simple user signed in", Toast.LENGTH_SHORT).show();
//                                    // Start User Activity
//                                    Intent launchUserActivity = new Intent(LoginActivity.this, UserActivity.class);
//                                    startActivity(launchUserActivity);
//
//                                } else if (userType.equals("company")) {
//                                    Toast.makeText(LoginActivity.this, "Company signed in!", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                    } else {
//                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//    }
//
//
//    private void initUIComponents() {
//        mEmailEditText = findViewById(R.id.emailLoginEditText);
//        mPasswordEditText = findViewById(R.id.passwordLoginEditText);
//        mLoginBtn = findViewById(R.id.loginSignInBtn);
////        mToolbar = findViewById(R.id.toolbar);
////        mToolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
//    }
//
//    private void initFirebaseComponents() {
//        mFirebaseAuth = FirebaseAuth.getInstance();
//    }
}

