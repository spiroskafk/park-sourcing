package com.spiroskafk.parking.activities.company;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.User;

import java.util.HashMap;

public class SetupCompany extends AppCompatActivity{

    // Auto-complete address
    private PlaceAutocompleteFragment autocompleteFragment;

    // UI elements
    private EditText mCompanyNameEt;
    private EditText mTelephoneEt;
    private Button mSetupBtn;

    // Location
    private LatLng location;
    private String locationName;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPrivateParkingRef;

    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_company);

        // Get data from CompanyDashboard
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = (User) extras.getSerializable("user");
        }


        init();

        // Setup listeners
        setupListeners();

    }

    private void init() {
        // Init - Setup toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init auto-complete address
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPrivateParkingRef = mFirebaseDatabase.getReference().child("private_parking");

        // Init UI elements
        mCompanyNameEt = findViewById(R.id.edittext_email_company);
        mTelephoneEt = findViewById(R.id.edittext_telphone_company);
        mSetupBtn = findViewById(R.id.button_setup_company);
    }

    private void setupListeners() {

        // Setup AutoComplete listener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.i(TAG, "Place: " + place.getName());
                location = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                locationName = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Button listener
        mSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mCompanyNameEt.getText().toString();
                String telephone = mTelephoneEt.getText().toString();
                if (location != null && locationName != null) {
                    if (!name.isEmpty() && !telephone.isEmpty()) {
                        setupCompany(name, telephone);
                    }
                } else {
                    Toast.makeText(SetupCompany.this, "You haven't filled in all the required details", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void setupCompany(String name, String telephone) {

        // Create Parking
        PrivateParking privateParking = new PrivateParking(name, locationName, user.getEmail(), "0",
                location.latitude, location.longitude, 0, 0, "0");
        mPrivateParkingRef.push().setValue(privateParking);
        Toast.makeText(SetupCompany.this, "You have successfully added your company!", Toast.LENGTH_SHORT).show();


    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
