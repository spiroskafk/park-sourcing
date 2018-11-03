package com.spiroskafk.parking;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.utils.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LeaveSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    public final String TAG = "LeaveSpotActivity";

    // GMap
    private GoogleMap mMap;

    // Firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mParkingSpotsDatabaseReference;
    private ChildEventListener mChildEventListener;

    // UI elements
    private Button mLeaveBtn;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // latlong
    private static float latit;
    private static float longtit;

    PowerMenu powerMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_spot);

        // Initialize phase
        init();

        final List<PowerMenuItem> list = new ArrayList<PowerMenuItem>();
        list.add(new PowerMenuItem("Θέση ΑΜΕΑ", false));
        list.add(new PowerMenuItem("Θέση Επισκεπτών", false));
        list.add(new PowerMenuItem("Δημοτικό Πάρκινγκ", false));
        list.add(new PowerMenuItem("Θέση Μόνιμης Κατοικίας", false));


        mLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerMenu = new PowerMenu.Builder(getApplicationContext())
                        .addItemList(list)
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER) // Animation start point (TOP | LEFT)
                        .setMenuRadius(10f)
                        .setMenuShadow(10f)
                        .setTextColor(getApplicationContext().getResources().getColor(R.color.black))
                        .setSelectedTextColor(Color.WHITE)
                        .setMenuColor(Color.WHITE)
                        .setSelectedMenuColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .build();

                powerMenu.showAtCenter(view);

            }


        });


    }

    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            Toast.makeText(getBaseContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
            powerMenu.setSelectedPosition(position); // change selected item
            powerMenu.dismiss();

            // Update database entry
            updateDatabase(item.getTitle());

        }
    };

    private void updateDatabase(String title) {
        // Gather the stuff and create the parking spot object
        // get lat, long
        getCurrentLocation();
        String address = getCompleteAddressString(latit, longtit);
        String id = UUID.randomUUID().toString();
        ParkingSpot newSpot = new ParkingSpot(id, title, address, latit, longtit);
        mParkingSpotsDatabaseReference.push().setValue(newSpot);
        Toast.makeText(LeaveSpotActivity.this, "Προστέθηκε νέα εγγραφή στη βάση", Toast.LENGTH_SHORT).show();
    }


    private void updateMap(float latit, float longit) {
        String address = getCompleteAddressString(latit, longit);
        LatLng coordinates = new LatLng(latit, longit);
        mMap.addMarker(new MarkerOptions()
                .position(coordinates)
                .title(address)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker)));
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

        // Init UI elements
        mLeaveBtn = findViewById(R.id.leave_btn);

        // Init Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mParkingSpotsDatabaseReference = mFirebaseDatabase.getReference().child("parking_spots");


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
