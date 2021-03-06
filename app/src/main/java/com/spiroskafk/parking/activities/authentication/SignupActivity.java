package com.spiroskafk.parking.activities.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.activities.company.CompanyDashboard;
import com.spiroskafk.parking.activities.user.UserActivity;
import com.spiroskafk.parking.model.User;

public class SignupActivity extends AppCompatActivity {

    // UI elements
    private EditText mName;
    private EditText mEmailAddress;
    private EditText mPassword;
    private CheckBox mUserCheckBbox;
    private CheckBox mCompanyCheckBox;
    private Button mSignup;
    private TextView mLogin;
    private ProgressBar mProgress;

    // Firebase vars
    private FirebaseAuth mAuth;

    private static String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        init();

        buttonListeners();

    }

    private void init() {
        mSignup = findViewById(R.id.btn_signup);
        mLogin = findViewById(R.id.textview_login);
        mName = findViewById(R.id.et_full_name);
        mEmailAddress = findViewById(R.id.et_email_address);
        mPassword = findViewById(R.id.et_password);
        mUserCheckBbox = findViewById(R.id.checkbox_register_user);
        mCompanyCheckBox = findViewById(R.id.checkbox_register_company);
        mProgress = findViewById(R.id.progressBar);
        mProgress.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
    }

    private void buttonListeners() {

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

        mUserCheckBbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCompanyCheckBox.setChecked(false);
            }
        });

        mCompanyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserCheckBbox.setChecked(false);
            }
        });
    }

    private void signup() {
        final String name = mName.getText().toString().trim();
        final String email = mEmailAddress.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && (mUserCheckBbox.isChecked() || mCompanyCheckBox.isChecked())) {
            mProgress.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (mUserCheckBbox.isChecked()) {
                            type = "user";
                        } else {
                            type = "company";
                        }

                        User user = new User(name, email, type, "Not Rated Yet", "0", 0, 0, false, 0, 0, 0);
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgress.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "You have successfulled registered a new " + type + " account", Toast.LENGTH_SHORT).show();
                                    if (mUserCheckBbox.isChecked()) {
                                        // launch NavUserActivity
                                        startActivity(new Intent(SignupActivity.this, UserActivity.class));
                                        finish();
                                    } else {
                                        startActivity(new Intent(SignupActivity.this, CompanyDashboard.class));
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(SignupActivity.this, "Unable to create new account", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        mProgress.setVisibility(View.GONE);
                    }
                }
            });
        }

    }
}
