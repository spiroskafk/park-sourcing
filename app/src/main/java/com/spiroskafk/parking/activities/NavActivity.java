package com.spiroskafk.parking.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.InfoWindowData;
import com.spiroskafk.parking.model.ParkingHouse;
import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.model.RentData;
import com.spiroskafk.parking.adapters.CustomInfoWindowAdapter;
import com.spiroskafk.parking.model.User;
import com.spiroskafk.parking.utils.Utils;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // Log TAG
    private static final String TAG = NavActivity.class.getSimpleName();

    // Google map
    private GoogleMap mMap;

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mParkingHouseRef;
    private DatabaseReference mRentedPlacesDatabaseReference;
    private DatabaseReference mParkingSpotRef;
    private DatabaseReference mUsersRef;
    private ChildEventListener mChildEventListener;
    private ChildEventListener mChildEventListener2;
    private ChildEventListener mChildEventListener3;

    // Auto-complete address
    private PlaceAutocompleteFragment autocompleteFragment;

    // Location Service
    private FusedLocationProviderClient mFusedLocationClient;

    // ParkingHouses
    private HashMap<String, ParkingHouse> parkingHouses;
    private HashMap<String, RentData> rentedHouses;
    private HashMap<String, String> markerToDBkeys;
    private HashMap<String, ParkingSpot> parkingSpots;

    // UI Components

    private CardView mLegendView;
    private Button mUnPark;

    // Parked street
    private String parkedStreet;

    // ParkingHouse id
    private String parkingHouseId;

    // CurrentUser
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        // Initialize phase
        init();

        // Setup listeners
        setupListeners();
    }


    private void init() {

        // Init - Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Init Google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Init auto-complete address
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Location Service
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init Firebase
        initFirebaseComponents();

        // Init HashMaps
        parkingHouses = new HashMap<String, ParkingHouse>();
        rentedHouses = new HashMap<String, RentData>();
        markerToDBkeys = new HashMap<String, String>();
        parkingSpots = new HashMap<String, ParkingSpot>();

        // Init UI
        mLegendView = findViewById(R.id.legend_cardview);
        mUnPark = findViewById(R.id.button_unpark);


    }

    private void setupListeners() {
        // Legend View
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLegendView.getVisibility() == View.INVISIBLE) {
                    mLegendView.setVisibility(View.VISIBLE);
                } else {
                    mLegendView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Get Current User
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user.isParked())
                    mUnPark.setVisibility(View.VISIBLE);
                else {
                    mUnPark.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Unpark
        mUnPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.isParked()) {
                    user.setParked(false);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("parked", false);
                    ref.updateChildren(data);
                    mUnPark.setVisibility(View.INVISIBLE);
                    updateParkingHouse(-1);
                }
            }
        });

        // Setup AutoComplete listener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        // Setup Firebase Database listener
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingHouse ph = dataSnapshot.getValue(ParkingHouse.class);
                parkingHouses.put(dataSnapshot.getKey(), ph);

                // If ParkingHouse is full, don't present it on map
                if (ph.getCapacity() == ph.getOccupied()) {
                    parkingHouses.remove(dataSnapshot.getKey());
                } else {
                    parkingHouses.put(dataSnapshot.getKey(), ph);
                }

                updateMap();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingHouse ph = dataSnapshot.getValue(ParkingHouse.class);

                // If ParkingHouse is full, don't present it on map
                if (ph.getCapacity() == ph.getOccupied()) {
                    parkingHouses.remove(dataSnapshot.getKey());
                } else {
                    parkingHouses.put(dataSnapshot.getKey(), ph);
                }
                updateMap();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                parkingHouses.remove(dataSnapshot.getKey());
                updateMap();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        mChildEventListener2 = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RentData rentData = dataSnapshot.getValue(RentData.class);
                rentedHouses.put(dataSnapshot.getKey(), rentData);
                updateMap();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RentData rentData = dataSnapshot.getValue(RentData.class);
                rentedHouses.put(dataSnapshot.getKey(), rentData);
                updateMap();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                rentedHouses.remove(dataSnapshot.getKey());
                updateMap();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        mChildEventListener3 = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingSpot spot = dataSnapshot.getValue(ParkingSpot.class);
                parkingSpots.put(dataSnapshot.getKey(), spot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingSpot spot = dataSnapshot.getValue(ParkingSpot.class);
                parkingSpots.put(dataSnapshot.getKey(), spot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                parkingSpots.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };


        mParkingHouseRef.addChildEventListener(mChildEventListener);
        mRentedPlacesDatabaseReference.addChildEventListener(mChildEventListener2);
        mParkingSpotRef.addChildEventListener(mChildEventListener3);
    }


    /**
     * Updates the ParkingHouse data that the user has just parked!
     * @param value
     */
    private void updateParkingHouse(int value) {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("parking_houses").child(parkingHouseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put("occupied", parkingHouses.get(parkingHouseId).getOccupied() + value);
            ref.updateChildren(data);
        }
    }


    private void updateMap() {
        mMap.clear();

        // Populate Rented Houses
        for (final HashMap.Entry<String, RentData> entry : rentedHouses.entrySet()) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit()))
                    .title(entry.getValue().getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            final InfoWindowData info = new InfoWindowData();
            info.setTitle("Rented Spot");
            info.setAddress(entry.getValue().getAddress());
            info.setSpaces(entry.getValue().getNos());

            // Get current location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                LatLng spotLocation = new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit());
                                // Calculate distance
                                Double meters = SphericalUtil.computeDistanceBetween(currentLocation, spotLocation);
                                //Log.i(TAG, "Meters: " + meters);
                                double m = Utils.round(meters/1000, 2);
                                info.setDistance(m + " km");
                            }
                        }
                    });

            //Set Custom InfoWindow Adapter
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(NavActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            Marker m = mMap.addMarker(marker);
            m.setTag(info);

            markerToDBkeys.put(m.getId(), entry.getKey());

        }

        // Populate Parking Houses
        for (final HashMap.Entry<String, ParkingHouse> entry : parkingHouses.entrySet()) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit()))
                    .title(entry.getValue().getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            Integer freeSpaces = entry.getValue().getCapacity() - entry.getValue().getOccupied();
            final InfoWindowData info = new InfoWindowData();
            info.setTitle("Parking House");
            info.setAddress(entry.getValue().getAddress());
            info.setSpaces(freeSpaces.toString());

            // Get current location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                LatLng spotLocation = new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit());
                                // Calculate distance
                                Double meters = SphericalUtil.computeDistanceBetween(currentLocation, spotLocation);
                                double m = Utils.round(meters/1000, 2);
                                info.setDistance(m + " km");
                            }
                        }
                    });

            //Set Custom InfoWindow Adapter
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(NavActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            Marker m = mMap.addMarker(marker);
            m.setTag(info);

            markerToDBkeys.put(m.getId(), entry.getKey());
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        // Get the marker id that User has pressed
        final String id = markerToDBkeys.get(marker.getId());

        // Get the ParkingHouse that the User has pressed
        final ParkingHouse house = parkingHouses.get(id);

        // If user is not parked, park his car here!
        if (house != null && !user.isParked()) {
            mParkingHouseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // Update ParkingHouse stats
                    updateParkingHouse(1);

                    parkedStreet = house.getAddress();

                    // Updates User status
                    updateUserStatus();

                    // Update ParkingHouseId
                    parkingHouseId = id;

                    // Find the user that has reported this position and update his points
                    String reportedSpotUserId = findUserReportedSpot(id);

                    // Handle: If the same user reported this position and parks here, don't award him points
                    if (!mAuth.getCurrentUser().getUid().equals(reportedSpotUserId)) {
                        rewardPoints(reportedSpotUserId);
                    }

                    mUnPark.setVisibility(View.VISIBLE);
                    Toast.makeText(NavActivity.this, "You have successfully parked at:  " + parkedStreet, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        } else {
            // Popup message that he is already parked
            Toast.makeText(NavActivity.this, "Your car is already parked at:  " + parkedStreet, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Updates User status on database
     */
    private void updateUserStatus() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> data = new HashMap<>();
        data.put("parked", true);
        ref.updateChildren(data);
        user.setParked(true);
    }



    /**
     * Find the user that reported this particular parking spot
     * @param houseId
     * @return userId
     */
    private String findUserReportedSpot(String houseId) {
        String userId = null;
        for (HashMap.Entry<String, ParkingSpot> entry : parkingSpots.entrySet()) {
            if (entry.getValue().getParkingHouseID().equals(houseId)) {

                long reportedTimestamp = entry.getValue().getTimestamp();
                long currentTimestamp = Instant.now().toEpochMilli();
                Date reportedDate = new Date(reportedTimestamp);
                Date currentDate = new Date(currentTimestamp);
                long minutesDiff = Utils.getDateDiff(reportedDate, currentDate, TimeUnit.MINUTES);

                // Only return userId if the time passed is less than 5 minutes
                if (minutesDiff <= 5) {
                    userId = entry.getValue().getUserID();

                    // Delete from database
                    mParkingSpotRef.child(entry.getKey()).removeValue();
                    return userId;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Updates user report points
     * @param userId
     */
    private void rewardPoints(final String userId) {
        // First get user data
        if (userId != null) {
            mUsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User userData = dataSnapshot.getValue(User.class);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("reports", userData.getReports() + 1);
                    ref.updateChildren(data);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    private void initFirebaseComponents() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mParkingHouseRef = mFirebaseDatabase.getReference().child("parking_houses");
        mRentedPlacesDatabaseReference = mFirebaseDatabase.getReference().child("rented_spots");
        mParkingSpotRef = mFirebaseDatabase.getReference().child("parking_spots");
        mUsersRef = mFirebaseDatabase.getReference().child("users");
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_report_spot) {
            startActivity(new Intent(NavActivity.this, ReportSpotActivity.class));
        } else if (id == R.id.nav_myprofile) {
            startActivity(new Intent(NavActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_rent_your_space) {
            startActivity(new Intent(NavActivity.this, RentYourPlace.class));
        } else if (id == R.id.nav_sign_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);

        // Get Last Known location
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Not needed for now
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
