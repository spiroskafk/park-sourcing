package com.spiroskafk.parking.activities.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.activities.SplashActivity;
import com.spiroskafk.parking.activities.company.CompanyActivity;
import com.spiroskafk.parking.activities.experimental.CompanyDashboard;
import com.spiroskafk.parking.activities.user.UserActivity;
import com.spiroskafk.parking.model.User;

public class SignInActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = SignInActivity.class.getSimpleName();

    // Sign in request code
    private static final int RC_SIGN_IN = 1;

    // Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private Firebase ref;

    // UI components
    private Button mGSignInButton;
    private Button mEmailSignInButton;
    private Button mRegisterButton;
    private GoogleSignInClient mGoogleSignInClient;
    private Boolean exit = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize phase
        init();

        //startActivity(new Intent(SignInActivity.this, SplashActivity.class));

        // Check if user is signed in, then proceed
        isSignedIn();


        // GSign in
        mGSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SignInActivity.this, "Needs fixing. Don't use it for now", Toast.LENGTH_SHORT).show();
                //signInWithGoogle();
            }
        });

        // Email Sign in
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch email sign in
                startActivity(new Intent(SignInActivity.this, EmailSignInActivity.class));
            }
        });

        // Register button
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch register
                startActivity(new Intent(SignInActivity.this, RegisterUserActivity.class));
            }
        });
    }


    private void init() {

        // Init UI
        initUIComponents();

//        Firebase.setAndroidContext(this.getApplicationContext());
//        ref = new Firebase("https://parking-application-e04ff.firebaseio.com/");
//
//        ref.unauth();

        // Init firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void isSignedIn() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.i(TAG, "mAuth user : " + mAuth.getCurrentUser().getUid());
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User currentUser = dataSnapshot.getValue(User.class);
                            if (currentUser != null) {
                                String userType = currentUser.getType();
                                if (userType != null) {
                                    if (userType.equals("user")) {
                                        //startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                        startActivity(new Intent(SignInActivity.this, SplashActivity.class));
                                    } else if (userType.equals("company")) {
                                        startActivity(new Intent(SignInActivity.this, CompanyDashboard.class));
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


    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Log.i(TAG, gso.getAccount().toString());

        // Start sign in
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Log.i(TAG, "Google sign in successful");

                // launch UserActivity
                startActivity(new Intent(SignInActivity.this, UserActivity.class));
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }


    private void initUIComponents() {
        mGSignInButton = findViewById(R.id.button_google_sign_in);
        mEmailSignInButton = findViewById(R.id.button_email_sign_in);
        mRegisterButton = findViewById(R.id.button_register_new_user);

    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finishAndRemoveTask(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
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
