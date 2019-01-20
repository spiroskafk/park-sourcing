package com.spiroskafk.parking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.spiroskafk.parking.R;

public class EmailSignInActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = EmailSignInActivity.class.getSimpleName();

    // UI elements
    private EditText mEmailEditText;
    private EditText mPswEditText;
    private Button mEmailSignInBtn;
    private CheckBox mUserCheckBbox;
    private CheckBox mCompanyCheckBox;

    // Firebase
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_in);


        init();

        // Checkbox listener
        mUserCheckBbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mCompanyCheckBox.setChecked(false);
            }
        });
        mCompanyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mUserCheckBbox.setChecked(false);
            }
        });

        // Sign in listener
        mEmailSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });
    }

    private void init() {

        // Init UI
        initUIComponents();

        // Init - Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void initUIComponents() {
        mEmailEditText = findViewById(R.id.edittext_email);
        mPswEditText = findViewById(R.id.edittext_password);
        mEmailSignInBtn = findViewById(R.id.button_sign_in);
        mUserCheckBbox = findViewById(R.id.checkbox_user);
        mCompanyCheckBox = findViewById(R.id.checkbox_company);
    }

    private void userLogin() {
        final String email = mEmailEditText.getText().toString().trim();
        final String password = mPswEditText.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty() && (mUserCheckBbox.isChecked() || mCompanyCheckBox.isChecked())) {
            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Toast.makeText(LoginActivity.this, "You have successfully signed in!", Toast.LENGTH_SHORT).show();
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String registeredUserID = currentUser.getUid();
                        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(registeredUserID);

                        mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String userType = dataSnapshot.child("type").getValue().toString();
                                if (userType.equals("user") && mUserCheckBbox.isChecked()) {
                                    Log.i(TAG, "User signed in");
                                    Toast.makeText(EmailSignInActivity.this, "User signed in", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(EmailSignInActivity.this, NavActivity.class));
                                } else if (userType.equals("company") && mCompanyCheckBox.isChecked()) {
                                    Toast.makeText(EmailSignInActivity.this, "Company signed in!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(EmailSignInActivity.this, CompanyNavActivity.class));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(EmailSignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
