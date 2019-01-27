package com.spiroskafk.parking.activities.user;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;
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
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.InfoWindowData;
import com.spiroskafk.parking.model.StreetParking;
import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.model.RentParking;
import com.spiroskafk.parking.adapters.CustomInfoWindowAdapter;
import com.spiroskafk.parking.model.User;
import com.spiroskafk.parking.utils.Permissions;
import com.spiroskafk.parking.utils.Utils;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // Log TAG
    private static final String TAG = UserActivity.class.getSimpleName();

    // Map component
    private GoogleMap mMap;

    // UI Components
    private CardView mLegendView;

    private long elapsedTime = 0;

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mStreetParkingRef;
    private DatabaseReference mRentedParkingRef;
    private DatabaseReference mParkingSpotRef;
    private DatabaseReference mPrivateHouseRef;
    private DatabaseReference mUsersRef;

    // Auto-complete address
    private PlaceAutocompleteFragment autocompleteFragment;

    // Location Service
    private FusedLocationProviderClient mFusedLocationClient;

    // HashMaps
    private HashMap<String, StreetParking> streetHouses;
    private HashMap<String, RentParking> rentedHouses;
    private HashMap<String, String> markerToDBkeys;
    private HashMap<String, ParkingSpot> parkingSpots;
    private HashMap<String, PrivateParking> privateHouses;

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

        // Read data from Firebase database
        readFromDatabase();

        // UI listeners
        registerUIListeners();
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

        // Init HashMaps
        streetHouses = new HashMap<String, StreetParking>();
        rentedHouses = new HashMap<String, RentParking>();
        markerToDBkeys = new HashMap<String, String>();
        parkingSpots = new HashMap<String, ParkingSpot>();
        privateHouses = new HashMap<String, PrivateParking>();

        // Location Service
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init Firebase
        initFirebaseComponents();

        // Init UI
        mLegendView = findViewById(R.id.legend_cardview);

    }


    /**
     * Setup callbacks in order to read data from database
     */
    private void readFromDatabase() {

        // Get user information
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    user = currentUser;

                    // TODO: Find out what's going on here!
                    if (user.getParkingHouseId().equals("0")) {
                        // Use the GPS lat/lng
                        if (!Permissions.Check_FINE_LOCATION(UserActivity.this)) {
                            Log.i(TAG, "Request location from GPS");
                            Permissions.Request_FINE_LOCATION(UserActivity.this, 22);
                            getCurrentLocation();
                        } else {
                            getCurrentLocation();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Populates streetHouses<String, StreetParking> HashMap
        mStreetParkingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null) {
                    streetHouses.put(dataSnapshot.getKey(), streetHouse);
                    updateMap();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null) {
                    if (user != null && user.isParked()) {
                        streetHouses.remove(dataSnapshot.getKey());
                    } else {
                        streetHouses.put(dataSnapshot.getKey(), streetHouse);
                    }
                    updateMap();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null) {
                    streetHouses.remove(dataSnapshot.getKey());
                    updateMap();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Populates rentHouses<String, RentParking> HashMap
        mRentedParkingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RentParking rentHouse = dataSnapshot.getValue(RentParking.class);
                if (rentHouse != null) {
                    rentedHouses.put(dataSnapshot.getKey(), rentHouse);
                    updateMap();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RentParking rentHouse = dataSnapshot.getValue(RentParking.class);
                if (rentHouse != null) {
                    rentedHouses.put(dataSnapshot.getKey(), rentHouse);
                    updateMap();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                rentedHouses.remove(dataSnapshot.getKey());
                updateMap();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RentParking rentHouse = dataSnapshot.getValue(RentParking.class);
                if (rentHouse != null) {
                    rentedHouses.remove(dataSnapshot.getKey());
                    updateMap();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Populates privateHouses<String, PrivateParking> HashMap
        mPrivateHouseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PrivateParking privateHouse = dataSnapshot.getValue(PrivateParking.class);
                if (privateHouse != null) {
                    privateHouses.put(dataSnapshot.getKey(), privateHouse);
                    updateMap();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PrivateParking privateHouse = dataSnapshot.getValue(PrivateParking.class);
                if (privateHouse != null) {
                    if (user != null && user.isParked()) {
                        privateHouses.remove(dataSnapshot.getKey());
                    } else {
                        privateHouses.put(dataSnapshot.getKey(), privateHouse);
                    }
                    updateMap();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                PrivateParking privateHouse = dataSnapshot.getValue(PrivateParking.class);
                if (privateHouse != null) {
                    privateHouses.remove(dataSnapshot.getKey());
                    updateMap();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Populates parkingSpots<String, ParkingSpot> HashMapp
        mParkingSpotRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingSpot spot = dataSnapshot.getValue(ParkingSpot.class);
                if (spot != null)
                    parkingSpots.put(dataSnapshot.getKey(), spot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ParkingSpot spot = dataSnapshot.getValue(ParkingSpot.class);
                if (spot != null)
                    parkingSpots.put(dataSnapshot.getKey(), spot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                ParkingSpot spot = dataSnapshot.getValue(ParkingSpot.class);
                if (spot != null)
                    parkingSpots.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    /**
     * Register UI listeners
     */
    private void registerUIListeners() {

        // Legend View
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLegendView.getVisibility() == View.INVISIBLE) mLegendView.setVisibility(View.VISIBLE);
                else mLegendView.setVisibility(View.INVISIBLE);
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

    }


    /**
     * Renders user on map with a custom marker
     */
    private void renderUserOnMap() {
        // Get latest user location
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(currentUser.getLatit(), currentUser.getLongtit()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker_icon));

                    final InfoWindowData info = new InfoWindowData();
                    CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(UserActivity.this);
                    mMap.setInfoWindowAdapter(adapter);

                    Marker m = mMap.addMarker(marker);
                    m.setTag(info);
                    m.setSnippet("user_info");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * Updates the ParkingHouse data that the user has just parked!
     * @param value
     */
    private void updateParkingHouse(int value) {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("street_parking").child(parkingHouseId);
            HashMap<String, Object> data = new HashMap<>();
            Log.i(TAG, "ParkingHouse: " + streetHouses.get(parkingHouseId).getAddress());
            data.put("occupied", streetHouses.get(parkingHouseId).getOccupied() + value);
            ref.updateChildren(data);
        }
    }

    /**
     * Updates the ParkingHouse data that the user has just parked!
     * @param value
     */
    private void updatePrivateHouse(int value) {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("private_parking").child(parkingHouseId);
            HashMap<String, Object> data = new HashMap<>();
            Log.i(TAG, "ParkingHouse: " + privateHouses.get(parkingHouseId).getAddress());
            data.put("occupied", privateHouses.get(parkingHouseId).getOccupied() + value);
            ref.updateChildren(data);
        }
    }


    private void updateMap() {
        long l1 = System.nanoTime();
        mMap.clear();


        // Space to Rent - Blue
        for (final HashMap.Entry<String, RentParking> entry : rentedHouses.entrySet()) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit()))
                    .title(entry.getValue().getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            final InfoWindowData info = new InfoWindowData();
            info.setAddress(entry.getValue().getAddress());
            info.setSpaces(entry.getValue().getNos());
            info.setFrom(entry.getValue().getFromDate());
            info.setUtil(entry.getValue().getUntilDate());
            info.setType(entry.getValue().getType());

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
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(UserActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            Marker m = mMap.addMarker(marker);
            m.setTag(info);
            m.setSnippet("space_to_rent");

            markerToDBkeys.put(m.getId(), entry.getKey());

        }

        // Street Parking
        for (final HashMap.Entry<String, StreetParking> entry : streetHouses.entrySet()) {
            // BUG: When freespots=1 and user parks, marker dissapears (parkinghouse removed from HashMap)
            // When user unparks, crashes
            // TEMP SOLUTION
            if (entry.getValue().getCapacity() == entry.getValue().getOccupied()) continue;
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit()))
                    .title(entry.getValue().getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            Integer freeSpaces = entry.getValue().getCapacity() - entry.getValue().getOccupied();
            final InfoWindowData info = new InfoWindowData();
            info.setTitle("Street Parking");
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
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(UserActivity.this);
            mMap.setInfoWindowAdapter(adapter);


            Marker m = mMap.addMarker(marker);
            m.setTag(info);
            m.setSnippet("street_parking");

            markerToDBkeys.put(m.getId(), entry.getKey());
        }


        // ParkingHouses (Private Parking)
        for (final HashMap.Entry<String, PrivateParking> entry : privateHouses.entrySet()) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit()))
                    .title(entry.getValue().getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            final InfoWindowData info = new InfoWindowData();
            info.setTitle("Parking House");
            info.setAddress(entry.getValue().getAddress());
            info.setCapacity(entry.getValue().getCapacity());
            info.setOccupied(entry.getValue().getOccupied());
            info.setHourlyCharge(entry.getValue().getHourlyCharge() + "€");
            info.setEntrance(entry.getValue().getEntrance() + "€");

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
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(UserActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            Marker m = mMap.addMarker(marker);
            m.setSnippet("parking_house");
            m.setTag(info);

            markerToDBkeys.put(m.getId(), entry.getKey());
        }

        if (user != null) {
            renderUserOnMap();
        }
    }

    
    @Override
    public void onInfoWindowClick(Marker marker) {

        String typeOfMarker = marker.getSnippet();

        // Get the marker id that User has pressed
        final String id = markerToDBkeys.get(marker.getId());

        if (typeOfMarker.equals("street_parking")) {
            // Get the ParkingHouse that the User has pressed
            final StreetParking house = streetHouses.get(id);

            // If user is not parked, park his car here!
            if (house != null && !user.isParked()) {
                mStreetParkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // Update ParkingHouseId
                        parkingHouseId = id;

                        // Update ParkingHouse stats
                        updateParkingHouse(1);

                        parkedStreet = house.getAddress();

                        // Updates User status
                        updateUserStatus();

                        // Find the user that has reported this position and update his points
                        String reportedSpotUserId = findUserReportedSpot(id);

                        // Handle: If the same user reported this position and parks here, don't award him points
                        if (!mAuth.getCurrentUser().getUid().equals(reportedSpotUserId)) {
                            rewardReports(reportedSpotUserId);
                            // Reward points
                            rewardPoints(parkingHouseId);
                        }

                        Toast.makeText(UserActivity.this, "You have successfully parked at:  " + parkedStreet, Toast.LENGTH_SHORT).show();

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            } else {
                // Popup message that he is already parked
                Toast.makeText(UserActivity.this, "Your car is already parked at:  " + house.getAddress(), Toast.LENGTH_SHORT).show();
            }
        } else if (typeOfMarker.equals("parking_house")) {
            // Get the ParkingHouse that the User has pressed
            final PrivateParking house = privateHouses.get(id);

            Log.i(TAG, "PrivateHouse: " + house.getAddress());

            // If user is not parked, park his car here!
            if (house != null && !user.isParked()) {
                mPrivateHouseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // Update ParkingHouseId
                        parkingHouseId = id;

                        // Update ParkingHouse stats
                        updatePrivateHouse(1);

                        parkedStreet = house.getAddress();

                        // Updates User status
                        updateUserStatus2();

                        // Find the user that has reported this position and update his points
                        String reportedSpotUserId = findUserReportedSpot(id);

                        // Handle: If the same user reported this position and parks here, don't award him points
                        if (!mAuth.getCurrentUser().getUid().equals(reportedSpotUserId)) {
                            rewardReports(reportedSpotUserId);
                        }

                        Toast.makeText(UserActivity.this, "You have successfully parked at:  " + parkedStreet, Toast.LENGTH_SHORT).show();

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            } else {
                // Popup message that he is already parked
                Toast.makeText(UserActivity.this, "Your car is already parked at:  " + house.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void rewardPoints(String houseId) {
        for (HashMap.Entry<String, ParkingSpot> entry : parkingSpots.entrySet()) {
            if (entry.getValue().getParkingHouseID().equals(houseId)) {
                // Parking Spot Id
                int rewardPoints = entry.getValue().getReward();
                // Update userID, with those reward points
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                HashMap<String, Object> data = new HashMap<>();
                data.put("rewardPoints", user.getRewardPoints() + rewardPoints);
                ref.updateChildren(data);
            }
        }
    }


    /**
     * Updates User status on database
     */
    private void updateUserStatus() {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            HashMap<String, Object> data = new HashMap<>();
            data.put("parked", true);
            data.put("parkingHouseId", parkingHouseId);
            data.put("latit", streetHouses.get(parkingHouseId).getLatit());
            data.put("longtit", streetHouses.get(parkingHouseId).getLongtit());
            ref.updateChildren(data);
            user.setParked(true);

            // Erase marker from map

        }
    }

    private void updateUserLatLng(double latit, double longtit) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> data = new HashMap<>();
        data.put("latit", latit);
        data.put("longtit", longtit);
        ref.updateChildren(data);
//        user.setLatit(latit);
//        user.setLongtit(longtit);
    }

    /**
     * Updates User status on database
     */
    private void updateUserStatus2() {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            HashMap<String, Object> data = new HashMap<>();
            data.put("parked", true);
            data.put("parkingHouseId", parkingHouseId);
            data.put("latit", privateHouses.get(parkingHouseId).getLatit());
            data.put("longtit", privateHouses.get(parkingHouseId).getLongtit());
            ref.updateChildren(data);
            user.setParked(true);

            // Erase marker from map

        }
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
    private void rewardReports(final String userId) {
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
        mStreetParkingRef = mFirebaseDatabase.getReference().child("street_parking");
        mRentedParkingRef = mFirebaseDatabase.getReference().child("rented_parking");
        mParkingSpotRef = mFirebaseDatabase.getReference().child("parking_spots");
        mPrivateHouseRef = mFirebaseDatabase.getReference().child("private_parking");
        mUsersRef = mFirebaseDatabase.getReference().child("users");
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_report_spot) {
            startActivity(new Intent(UserActivity.this, ReportSpotActivity.class));
        } else if (id == R.id.nav_myprofile) {
            startActivity(new Intent(UserActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_rent_your_space) {
            startActivity(new Intent(UserActivity.this, RentYourPlace.class));
        } else if (id == R.id.nav_reward) {
            startActivity(new Intent(UserActivity.this, RewardsActivity.class));
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

        if (!Permissions.Check_FINE_LOCATION(UserActivity.this)) {
            //if not permisson granted so request permisson with request code
            Permissions.Request_FINE_LOCATION(UserActivity.this, 22);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                            } else {

                            }
                        }
                    });
        }
    }

    private void getCurrentLocation() {
        // Get current location
        if (!Permissions.Check_FINE_LOCATION(UserActivity.this)) {
            //if not permisson granted so request permisson with request code
            Permissions.Request_FINE_LOCATION(UserActivity.this, 22);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                updateUserLatLng(location.getLatitude(), location.getLongitude());
                                renderUserOnMap();
                            }
                        }
                    });
        }
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


    /**
     * Not Used For Now
     * @param v
     */
    public void showPopup(View v) {
        final Dialog myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.custom_popup);
        TextView txtClose = myDialog.findViewById(R.id.close_tv);


        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

    }

}
