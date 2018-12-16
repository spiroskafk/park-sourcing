package com.spiroskafk.parking.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.maps.android.SphericalUtil;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.StreetParking;
import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.model.User;
import com.spiroskafk.parking.utils.Permissions;
import com.spiroskafk.parking.utils.Utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReportSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    private static final String TAG = ReportSpotActivity.class.getSimpleName();

    // GMap
    private GoogleMap mMap;

    // HashMaps
    private HashMap<String, StreetParking> streetHouses;
    private HashMap<String, ParkingSpot> parkingSpots;

    // Firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mStreetParkingRef;
    private DatabaseReference mParkingSpotDBRef;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFunctions mFunctions;

    // UI elements
    private Button mLeaveBtn;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // latlong
    private static float latit;
    private static float longtit;

    // Parking Houses
    private HashMap<String, StreetParking> houses;


    // Holds the userID that reports the current position as free
    private String userID;

    PowerMenu powerMenu;

    private ArrayList<String> items;

    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_spot);

        // Initialize phase
        init();


        setupListeners();
    }



    private void setupListeners() {

        // Get User current state from database
        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Listener For StreetHouses
        mStreetParkingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null) {
                    streetHouses.put(dataSnapshot.getKey(), streetHouse);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null) {
                    streetHouses.put(dataSnapshot.getKey(), streetHouse);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null) {
                    streetHouses.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        // Report Free Spot
        mLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get elapsed time since lastReportedTimestamp
                long lastReportedTimestamp = user.getLastReportTimestamp();
                final long currentTimestamp = Instant.now().toEpochMilli();
                Date reportedDate = new Date(lastReportedTimestamp);
                Date currentDate = new Date(currentTimestamp);
                long minutesDiff = Utils.getDateDiff(reportedDate, currentDate, TimeUnit.MINUTES);

                // Don't allow user to continually report positions
                if (minutesDiff < 2) {
                    Toast.makeText(ReportSpotActivity.this, "You have already reported a parking spot. You can't report so soon again!", Toast.LENGTH_SHORT).show();
                    return;
                }

                reportParkingSpot(currentTimestamp);
            }
        });
    }


    /**
     * Report a parking spot
     * @param currentTimestamp
     */
    private void reportParkingSpot(final long currentTimestamp) {

        if (!user.getParkingHouseId().equals("0")) {
            String parkingHouseId = user.getParkingHouseId();

            // Update parkinghouse data
            updateParkingHouse(parkingHouseId);

            // Update user data
            updateUserData(currentTimestamp, parkingHouseId);

            // Create new entry in database
            createParkingSpot(parkingHouseId, currentTimestamp);

            Toast.makeText(ReportSpotActivity.this, "You have just reported a free spot at: " + streetHouses.get(parkingHouseId).getAddress(), Toast.LENGTH_SHORT).show();

        } else {

            // User is not parked in a parking house, so we promt a
            // popup window with possible roads he is leaving from

            // Collection that holds address, parkingHousesIds
            final HashMap<String, String> addressToKey = new HashMap<String, String>();

            // Popup user with the closest streets
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ReportSpotActivity.this);
            builderSingle.setIcon(R.drawable.icon_info_window);
            builderSingle.setTitle("Choose Street");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ReportSpotActivity.this, android.R.layout.select_dialog_singlechoice);

            // Calculate closest houses
            HashMap<String, StreetParking> closestHouses = calculateUpTwoFour(new LatLng(latit, longtit));

            if (closestHouses.size() == 0) {
                Toast.makeText(ReportSpotActivity.this, "There isn't any StreetHouse close to you, in order to report a free spot!", Toast.LENGTH_SHORT).show();
                return;
            }

            for (HashMap.Entry<String, StreetParking> entry : closestHouses.entrySet()) {
                arrayAdapter.add(entry.getValue().getAddress());
                addressToKey.put(entry.getValue().getAddress(), entry.getKey());
            }

            builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String strName = arrayAdapter.getItem(which);
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(ReportSpotActivity.this);
                    builderInner.setMessage(strName);
                    builderInner.setTitle("You are leaving from street: ");
                    builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            // Report Free Spot
                            Log.i(TAG, "Spot Leaving: " + addressToKey.get(strName));

                            updateParkingHouse(addressToKey.get(strName));

                            updateUserData(currentTimestamp, addressToKey.get(strName));

                            createParkingSpot(addressToKey.get(strName), currentTimestamp);

                            dialog.dismiss();
                        }
                    });
                    builderInner.show();
                }
            });
            builderSingle.show();
        }
    }

    private void updateParkingHouse(String parkingHouseId) {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("street_parking").child(parkingHouseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put("occupied", streetHouses.get(parkingHouseId).getOccupied() - 1);
            ref.updateChildren(data);
        }
    }


    /**
     * Creates a new ParkingSpot
     * @param parkingHouseID
     */
    private void createParkingSpot(String parkingHouseID, long timestamp) {

        ParkingSpot spot = new ParkingSpot(streetHouses.get(parkingHouseID).getLatit(),
                streetHouses.get(parkingHouseID).getLongtit(), 5, timestamp, parkingHouseID, userID);

        // Push spot to database
        String key = mParkingSpotDBRef.push().getKey();
        mParkingSpotDBRef.push().setValue(spot);

        // Update ParkingSpots HashMap
        parkingSpots.put(key, spot);
    }


    /**
     * Updates User status
     */
    private void updateUserData(long timestamp, String parkingHouseId) {

        // Update user table
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> data = new HashMap<>();
        data.put("lastReportTimestamp", timestamp);
        data.put("parkingHouseId", "0");
        ref.updateChildren(data);

    }



    /**
     * @return HashMap<parkingHouseKey, ParkingHouseObject> with the shortest distance
     */
    private HashMap<String, StreetParking> calculateUpTwoFour(LatLng currentLoc) {
        String nodeId = "";
        StreetParking shortestHouse = new StreetParking();
        double meters;
        HashMap<String, StreetParking> house = new HashMap<>();

        Log.i(TAG, "houses: " + streetHouses.toString());;

        for (HashMap.Entry<String, StreetParking> entry : streetHouses.entrySet()) {
            LatLng houseLoc = new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit());
            meters = SphericalUtil.computeDistanceBetween(currentLoc, houseLoc);
            Log.i(TAG, "meters : " + meters);
            if (meters < 1000) {
                nodeId = entry.getKey();
                shortestHouse = entry.getValue();
                house.put(nodeId, shortestHouse);
            }
        }

        return house;
    }


    private void updateMap(float latit, float longtit) {
        String address = Utils.getStreetAddress(latit, longtit, this);
        LatLng coordinates = new LatLng(latit, longtit);
        mMap.addMarker(new MarkerOptions()
                .position(coordinates)
                .title(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 14));


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

        // Init collections - HashMaps
        streetHouses = new HashMap<String, StreetParking>();
        parkingSpots = new HashMap<String, ParkingSpot>();

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mParkingSpotDBRef = mFirebaseDatabase.getReference().child("parking_spots");
        mStreetParkingRef = mFirebaseDatabase.getReference().child("street_parking");
        mUsersRef = mFirebaseDatabase.getReference().child("users").child(mAuth.getCurrentUser().getUid());
        mFunctions = FirebaseFunctions.getInstance();

        // Init google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Helper method to create temp ParkingHouses
     */
    private void createParkingHouse() {
        // Create ParkingHouse - Mikinon
        HashMap<Double, Double> latsLngs = new HashMap<Double, Double>();

        latsLngs.put(38.224410, 21.721600);
        latsLngs.put(38.231839, 21.725454);
        latsLngs.put(38.235869, 21.746381);
        latsLngs.put(38.238691, 21.744053);
        latsLngs.put(38.244465, 21.732212);
        latsLngs.put(38.248383, 21.739411);

        latsLngs.put(38.245822, 21.743312);
        latsLngs.put(38.253913, 21.737745);
        latsLngs.put(38.255914, 21.746038);
        latsLngs.put(38.258744, 21.742634);
        latsLngs.put(38.261620, 21.743372);

        latsLngs.put(38.261178, 21.754756);
        latsLngs.put(38.264316, 21.754430);
        latsLngs.put(38.274776, 21.743368);
        latsLngs.put(38.278184, 21.751631);
        latsLngs.put(38.286659, 21.778331);


        latsLngs.put(38.286659, 21.778331);
        latsLngs.put(38.286659, 21.778331);
        latsLngs.put(38.286659, 21.778331);
        latsLngs.put(38.286659, 21.778331);


        for (HashMap.Entry<Double, Double> entry : latsLngs.entrySet()) {
            String address = Utils.getStreetAddress(entry.getKey(), entry.getValue(), this);
            String id = UUID.randomUUID().toString();
            int capacity = (int )(Math.random() * 20 + 10);
            int occupied = (int )(Math.random() * 5 + 5);
            int points = (int )(Math.random() * 10 + 1);
            StreetParking ph = new StreetParking(entry.getKey(), entry.getValue(), address, id, "Whatever", capacity, occupied, points);
            mStreetParkingRef.push().setValue(ph);
        }

        //Push to db
        //mDbRef.push().setValue(ph);
        //Toast.makeText(ReportSpotActivity.this, "Προστέθηκε νέα εγγραφή στη βάση", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
