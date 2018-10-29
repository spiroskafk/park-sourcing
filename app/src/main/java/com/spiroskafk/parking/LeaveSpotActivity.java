package com.spiroskafk.parking;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.utils.Permissions;

import java.util.List;
import java.util.Locale;

public class LeaveSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    public final String TAG = "LeaveSpotActivity";

    // GMap
    private GoogleMap mMap;

    // Firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mParkingSpotsDatabaseReference;
    private ChildEventListener mChildEventListener;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // latlong
    private static float latit;
    private static float longtit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_spot);

        // Initialize phase
        init();
    }


    private void updateMap(float latit, float longit) {
        String address = getCompleteAddressString(latit, longit);
        LatLng coordinates = new LatLng(latit, longit);
        mMap.addMarker(new MarkerOptions()
                .position(coordinates)
                .title(address)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.park)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // getLocation
        getCurrentLocation();
    }


    private void getCurrentLocation() {
        // Get current location
        if (!Permissions.Check_FINE_LOCATION(LeaveSpotActivity.this)) {
            //if not permisson granted so request permisson with request code
            Permissions.Request_FINE_LOCATION(LeaveSpotActivity.this, 22);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latit = (float) location.getLatitude();
                                longtit = (float) location.getLongitude();
                                updateMap(latit, longtit);
                            } else {

                            }
                        }
                    });
        }
    }

    private void init() {
        // Init - Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.i(TAG, "Address = " + strReturnedAddress.toString());
            } else {
                Log.i(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Cannot get Address!");
        }
        return strAdd;
    }
}
