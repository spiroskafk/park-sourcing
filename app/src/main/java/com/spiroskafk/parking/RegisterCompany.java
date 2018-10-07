package com.spiroskafk.parking;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.spiroskafk.parking.model.Company;

public class RegisterCompany extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mAddressEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_company);

        // Initialize UI components
        initUIComponents();

        // Init firebase
        initFirebaseComponents();

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerCompany();
            }
        });
    }

    private void registerCompany() {
        final String name = mNameEditText.getText().toString();
        final String address = mAddressEditText.getText().toString();
        final String email = mEmailEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !address.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create new company
                                Company comp = new Company(name, address, email, "company");
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(comp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mProgressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterCompany.this, "You have successfulled registered", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // display failure message
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterCompany.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }



    private void initUIComponents() {
        mNameEditText = findViewById(R.id.nameEditText);
        mAddressEditText = findViewById(R.id.addressEditText);
        mEmailEditText = findViewById(R.id.emailEditText);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mRegisterButton = findViewById(R.id.registerBtn);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
    }

    private void initFirebaseComponents() {
        mFirebaseAuth = FirebaseAuth.getInstance();
    }
}
