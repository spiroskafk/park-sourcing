package com.spiroskafk.parking;


import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.utils.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportPositionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mParkingSpotsDatabaseReference;

    // UI components
    private Spinner spinner;
    private TextView latTv;
    private TextView longTv;
    private Button mSubmitBtn;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;

    private static String id;
    private static String category;
    private static float latitude;
    private static float longitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_position);

        initFirebaseComponents();
        initUIComponenets();

        // Init Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Spinner drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("On the road");
        categories.add("Public parking spot");

        // Creating adapter
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);


        if(!Permissions.Check_FINE_LOCATION(ReportPositionActivity.this))
        {
            //if not permisson granted so request permisson with request code
            Permissions.Request_FINE_LOCATION(ReportPositionActivity.this,22);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latTv.setVisibility(View.VISIBLE);
                                longTv.setVisibility(View.VISIBLE);
                                latTv.setText("Latitude : " + location.getLatitude());
                                longTv.setText("Longitude : " + location.getLongitude());

                                // populate lat,long
                                latitude = (float)location.getLatitude();
                                longitude = (float)location.getLongitude();
                            } else {

                            }
                        }
                    });

        }

        // Submit button listener
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gather the stuff and create the parking spot object
                // generate uid
                id = UUID.randomUUID().toString();
                ParkingSpot newSpot = new ParkingSpot(id, category, latitude, longitude);
                mParkingSpotsDatabaseReference.push().setValue(newSpot);
            }
        });





    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Toast.makeText(ReportPositionActivity.this, "You've just selected an item, you badass!", Toast.LENGTH_SHORT).show();
        category = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void initFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mParkingSpotsDatabaseReference = mFirebaseDatabase.getReference().child("parking_spots");
    }

    private void initUIComponenets() {
        spinner = findViewById(R.id.spinner);
        latTv = findViewById(R.id.latTv);
        longTv = findViewById(R.id.longTv);
        mSubmitBtn = findViewById(R.id.submitBtn);
    }


}
