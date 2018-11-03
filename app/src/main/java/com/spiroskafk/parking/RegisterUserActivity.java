package com.spiroskafk.parking;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.model.User;


public class RegisterUserActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = "RegisterUserActivity";

    // UI elements
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;

    // Firebase Components
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Initialize phase
        init();

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

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create new user
                                User user = new User(name, email, "simple_user");
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mProgressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterUserActivity.this, "You have successfulled registered", Toast.LENGTH_SHORT).show();
                                            finish();
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
        mNameEditText = findViewById(R.id.reg_display_name);
        mEmailEditText = findViewById(R.id.reg_email);
        mPasswordEditText = findViewById(R.id.reg_password);
        mRegisterButton = findViewById(R.id.reg_btn);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
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
