package com.spiroskafk.parking.activities.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.activities.company.CompanyActivity;
import com.spiroskafk.parking.activities.experimental.CompanyDashboard;
import com.spiroskafk.parking.activities.user.UserActivity;
import com.spiroskafk.parking.model.User;


public class RegisterUserActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = RegisterUserActivity.class.getSimpleName();

    // UI elements
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;
    private CheckBox mUserCheckBbox;
    private CheckBox mCompanyCheckBox;

    // Firebase Components
    private FirebaseAuth mFirebaseAuth;

    // Rest
    private static String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Initialize phase
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

        // Register
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();
        final String password = mPasswordEditText.getText().toString().trim();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && (mUserCheckBbox.isChecked() || mCompanyCheckBox.isChecked())) {
            mProgressBar.setVisibility(View.VISIBLE);
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create new user
                                if (mUserCheckBbox.isChecked()) {
                                    type = "user";
                                } else {
                                    type = "company";
                                }

                                // Register new user
                                User user = new User(name, email, type, "Not Rated Yet", null, 0, 0, false, 0, 0, 0);
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mProgressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterUserActivity.this, "You have successfulled registered a new " + type + " account", Toast.LENGTH_SHORT).show();
                                            if (mUserCheckBbox.isChecked()) {
                                                // launch NavUserActivity
                                                finish();
                                                startActivity(new Intent(RegisterUserActivity.this, UserActivity.class));
                                            } else {
                                                finish();
                                                startActivity(new Intent(RegisterUserActivity.this, CompanyDashboard
                                                        .class));
                                            }
                                        } else {
                                            // display failure message
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterUserActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });

        }
    }

    private void init() {
        // Init UI
        initUIComponents();

        // Init firebase
        initFirebaseComponents();

        // Init - Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initUIComponents() {
        mNameEditText = findViewById(R.id.edittext_register_name);
        mEmailEditText = findViewById(R.id.edittext_register_email);
        mPasswordEditText = findViewById(R.id.edittext_register_password);
        mRegisterButton = findViewById(R.id.button_register);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        mUserCheckBbox = findViewById(R.id.checkbox_register_user);
        mCompanyCheckBox = findViewById(R.id.checkbox_register_company);
    }

    private void initFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
