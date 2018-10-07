package com.spiroskafk.parking;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by Gino Osahon on 03/03/2017.
 */

// This class handles Google Firebase Authentication and also saves the user details to Firebase
public class RegisterActivity extends AppCompatActivity {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;

    private EditText mDisplayName, mPassword, mEmail;
    private Button mBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Init UI components
        mAuth = FirebaseAuth.getInstance();
        mDisplayName =  (EditText) findViewById(R.id.reg_display_name);
        mEmail =  (EditText)findViewById(R.id.reg_email);
        mPassword =  (EditText)findViewById(R.id.reg_password);
        mBtn = findViewById(R.id.reg_btn);

        mProgressDialog = new ProgressDialog(this);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mProgressDialog.setTitle("Loading...");
                    mProgressDialog.setMessage("Please wait!");
                    mProgressDialog.setCanceledOnTouchOutside(true);
                    mProgressDialog.show();
                    register(name, email, password);
                }
            }

            private void register(final String name, final String email, final String password) {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("email", email);
                            userMap.put("password", password);

                            mDatabase.setValue(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Intent launch = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(launch);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
