package com.spiroskafk.parking;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;



public class MainActivity extends AppCompatActivity {

    // UI Components
    private Button mRegisterUserBtn;
    private Button mRegisterCompBtn;
    private Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initUIComponents();

        // Set up button listeners
        mRegisterUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch RegisterActivity
                Intent launchRegisterUserActivity = new Intent(MainActivity.this, RegisterUserActivity.class);
                startActivity(launchRegisterUserActivity);

            }
        });

        mRegisterCompBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch RegisterCompany
                Intent launchRegisterCompActivity = new Intent(MainActivity.this, RegisterCompany.class);
                startActivity(launchRegisterCompActivity);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch LoginActivity
                Intent launchLoginActivity = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(launchLoginActivity);
            }
        });
    }

    private void initUIComponents() {
        mRegisterUserBtn = findViewById(R.id.registerUserBtn);
        mRegisterCompBtn = findViewById(R.id.registerCompBtn);
        mLoginBtn = findViewById(R.id.loginBtn);
    }
}
