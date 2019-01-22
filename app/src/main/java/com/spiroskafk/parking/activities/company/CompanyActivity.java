package com.spiroskafk.parking.activities.company;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.spiroskafk.parking.adapters.CustomInfoWindowAdapter;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.InfoWindowData;
import com.spiroskafk.parking.model.User;
import com.spiroskafk.parking.utils.Utils;

import java.util.HashMap;

public class CompanyActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    // Log TAG
    private static final String TAG = CompanyActivity.class.getSimpleName();

    // Google map
    private GoogleMap mMap;

    private static double latit;
    private static double longtit;

    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDbRef;
    private FirebaseAuth mAuth;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    private HashMap<String, PrivateParking> privateHouses;
    private String houseId;

    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_nav);

        // Initialize phase
        init();

        //createPrivateHouse();

        // Setup listeners
        setupListeners();
    }

    private void init() {
        // Init - Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        privateHouses = new HashMap<String, PrivateParking>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDbRef = mFirebaseDatabase.getReference().child("private_houses");
        mAuth = FirebaseAuth.getInstance();

        // Init google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void setupListeners() {

        // Get company info
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null)
                    user = currentUser;
                    Log.i(TAG, "User: " + user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Find out which PrivateParking
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("private_parking");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                PrivateParking parking = dataSnapshot.getValue(PrivateParking.class);
//                if (parking != null) {
//                    Log.i(TAG, "ParkingHouse: " + parking.getEmail());
//                    //Log.i(TAG, "parking : " + parking.getEmail().toString());
////                    if (parking.getEmail().equals(user.getEmail())) {
////                        privateHouses.put(dataSnapshot.getKey(), parking);
////                        houseId = dataSnapshot.getKey();
////                        updateMap();
////                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PrivateParking parking = dataSnapshot.getValue(PrivateParking.class);
                if (parking != null && user != null) {
                    Log.i(TAG, "ParkingHouse: " + parking.getEmail());
                    //Log.i(TAG, "parking : " + parking.getEmail().toString());
                    if (parking.getEmail().equals(user.getEmail())) {
                        privateHouses.put(dataSnapshot.getKey(), parking);
                        houseId = dataSnapshot.getKey();
                        updateMap();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });









//        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                PrivateParking comp = dataSnapshot.getValue(PrivateParking.class);
//                Log.i(TAG, "Latit: " + comp.getLatit());
//                Log.i(TAG, "mail: " + comp.getEmail());
//                privateHouses.put(dataSnapshot.getKey(), comp);
//                houseId = dataSnapshot.getKey();
//                updateMap();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        mDbRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                PrivateParking comp = dataSnapshot.getValue(PrivateParking.class);
//                Log.i(TAG, "Latit: " + comp.getLatit());
//                Log.i(TAG, "mail: " + comp.getEmail());
//                privateHouses.put(dataSnapshot.getKey(), comp);
//                houseId = dataSnapshot.getKey();
//                updateMap();
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



    }

    private void updateMap() {
        // Update map
        Log.i(TAG, "houseID: " + houseId);
        if (privateHouses != null) {
            Log.i(TAG, "LATIT: " + privateHouses.get(houseId).getLatit());
            LatLng coordinates = new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit());
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit()))
                    .title(privateHouses.get(houseId).getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            final InfoWindowData info = new InfoWindowData();
            info.setAddress(privateHouses.get(houseId).getAddress());
            info.setCapacity(privateHouses.get(houseId).getCapacity());
            info.setOccupied(privateHouses.get(houseId).getOccupied());
            info.setHourlyCharge(privateHouses.get(houseId).getHourlyCharge());

            // Get current location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                LatLng spotLocation = new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit());
                                // Calculate distance
                                Double meters = SphericalUtil.computeDistanceBetween(currentLocation, spotLocation);
                                double m = Utils.round(meters/1000, 2);
                                info.setDistance(m + " km");
                            }
                        }
                    });

            //Set Custom InfoWindow Adapter
            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(CompanyActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            Marker m = mMap.addMarker(marker);
            m.setSnippet("Private");
            m.setTag(info);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit()), 14));
        }

    }

    private void createPrivateHouse() {
        // Create new company
        String address = Utils.getStreetAddress(38.211891, 21.730654, this);
        PrivateParking comp = new PrivateParking("Argyros Parking", address, "comp@gmail.com", "4", 38.211891, 21.730654, 20, 12,  "4");

        // add to firebase
        mDbRef.push().setValue(comp);
    }







    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (item.getItemId() == R.id.menu_update) {
            startActivity(new Intent(CompanyActivity.this, InventoryActivity.class));
        } else if (id == R.id.nav_sign_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        return super.onOptionsItemSelected(item);
    }
}
