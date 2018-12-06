package com.spiroskafk.parking.activities;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.ParkingHouse;
import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.utils.Permissions;
import com.spiroskafk.parking.utils.Utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReportSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    private static final String TAG = ReportSpotActivity.class.getSimpleName();

    // GMap
    private GoogleMap mMap;

    // Firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDbRef;
    private DatabaseReference mParkingSpotDBRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    // UI elements
    private Button mLeaveBtn;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // latlong
    private static float latit;
    private static float longtit;

    // Parking Houses
    private HashMap<String, ParkingHouse> houses;
    // Parking Spots
    private HashMap<String, ParkingSpot> parkingSpots;

    // Holds the userID that reports the current position as free
    private String userID;

    // Holds the ParkingHouse that this position will be assigned to
    private String parkingHouseID;

    PowerMenu powerMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_spot);

        // Initialize phase
        init();

        final List<PowerMenuItem> list = new ArrayList<PowerMenuItem>();
        list.add(new PowerMenuItem("Θέση ΑΜΕΑ", false));
        list.add(new PowerMenuItem("Θέση Επισκεπτών", false));
        list.add(new PowerMenuItem("Δημοτικό Πάρκινγκ", false));
        list.add(new PowerMenuItem("Θέση Μόνιμης Κατοικίας", false));


        // Popup window list
        mLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerMenu = new PowerMenu.Builder(getApplicationContext())
                        .addItemList(list)
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
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
            powerMenu.setSelectedPosition(position);
            powerMenu.dismiss();

            // Update database entry
            updateDatabase(item.getTitle());
        }
    };

    /**
     * This method gets called when you hit the leave (parkHere) button
     *
     * @param title: The type of the position that is released
     */
    private void updateDatabase(String title) {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            // Update Parking Houses table
                            mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // Update parking houses
                                    houses = collectParkingHouses(dataSnapshot);
                                    LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                                    HashMap<String, ParkingHouse> shortestHouse = calculateDistance(currentLoc);
                                    updateShortestHouseData(shortestHouse);

                                    // Update parking spots with new entry
                                    parkingHouseID = shortestHouse.keySet().toArray()[0].toString();
                                    Log.i(TAG, "ParkingHouseID: " + parkingHouseID);

                                    // Get Timestamp
                                    long epoch = Instant.now().toEpochMilli();

                                    // Add new entry
                                    ParkingSpot spot = new ParkingSpot(location.getLatitude(), location.getLongitude(),
                                            5, epoch, parkingHouseID, userID);

                                    String key = mParkingSpotDBRef.push().getKey();
                                    mParkingSpotDBRef.push().setValue(spot);
                                    parkingSpots.put(key, spot);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });

                        }
                    }
                });
    }

    /**
     * User is leaving the parking so we are updating the free spots
     * @param house
     */
    private void updateShortestHouseData(HashMap<String, ParkingHouse> house) {
        for (HashMap.Entry<String, ParkingHouse> entry : house.entrySet()) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("parking_houses").child(entry.getKey());
            HashMap<String, Object> data = new HashMap<>();
            data.put("occupied", entry.getValue().getOccupied() - 1);
            ref.updateChildren(data);
        }

    }

    /**
     * @return house with the shortest distance
     */
    private HashMap<String, ParkingHouse> calculateDistance(LatLng currentLoc) {
        double shortestDist = 100000;
        String nodeId = "";
        ParkingHouse shortestHouse = new ParkingHouse();
        double meters;
        HashMap<String, ParkingHouse> house = new HashMap<>();

        for (HashMap.Entry<String, ParkingHouse> entry : houses.entrySet()) {
            LatLng houseLoc = new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit());
            meters = SphericalUtil.computeDistanceBetween(currentLoc, houseLoc);
            if (meters < shortestDist) {
                shortestDist = meters;
                nodeId = entry.getKey();
                shortestHouse = entry.getValue();
            }
        }

        house.put(nodeId, shortestHouse);
        return house;
    }

    private HashMap<String, ParkingHouse> collectParkingHouses(@NonNull DataSnapshot dataSnapshot) {
        HashMap<String, ParkingHouse> houses = new HashMap<>();
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            houses.put(postSnapshot.getKey(), postSnapshot.getValue(ParkingHouse.class));
        }

        return houses;
    }


    private void updateMap(float latit, float longtit) {
        String address = Utils.getStreetAddress(latit, longtit, this);
        LatLng coordinates = new LatLng(latit, longtit);
        mMap.addMarker(new MarkerOptions()
                .position(coordinates)
                .title(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
        if (!Permissions.Check_FINE_LOCATION(ReportSpotActivity.this)) {
            //if not permisson granted so request permisson with request code
            Permissions.Request_FINE_LOCATION(ReportSpotActivity.this, 22);
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
        mLeaveBtn = findViewById(R.id.button_leave);

        // Init Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDbRef = mFirebaseDatabase.getReference().child("parking_houses");
        mParkingSpotDBRef = mFirebaseDatabase.getReference().child("parking_spots");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();


        // Init google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        parkingSpots = new HashMap<String, ParkingSpot>();
    }

    /**
     * Helper method to create temp ParkingHouses
     */
    private void createParkingHouse(String type) {
        // Create ParkingHouse - Mikinon
        latit = 38.214244f;
        longtit = 21.740157f;

        String address = Utils.getStreetAddress(latit, longtit, this);
        String id = UUID.randomUUID().toString();
        ParkingHouse ph = new ParkingHouse(latit, longtit, address, id, type, 30, 5, 10);

        //Push to db
        mDbRef.push().setValue(ph);
        Toast.makeText(ReportSpotActivity.this, "Προστέθηκε νέα εγγραφή στη βάση", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
