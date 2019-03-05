package com.spiroskafk.parking.activities.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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
import com.spiroskafk.parking.model.ParkingSpot;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.RentParking;
import com.spiroskafk.parking.model.StreetParking;
import com.spiroskafk.parking.model.User;
import com.spiroskafk.parking.utils.Permissions;
import com.spiroskafk.parking.utils.Utils;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ReportSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    private static final String TAG = ReportSpotActivity.class.getSimpleName();

    // Map component
    private GoogleMap mMap;

    // Firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mStreetParkingRef;
    private DatabaseReference mPrivateParkingRef;
    private DatabaseReference mRentedParkingRef;
    private DatabaseReference mParkingSpotDBRef;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;

    // UI components
    private Button mLeaveBtn;

    // Application context
    private Context mContext;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // User location
    private static float latit;
    private static float longtit;

    // HashMaps
    private HashMap<String, PrivateParking> privateHouses;
    private HashMap<String, StreetParking> streetHouses;
    private HashMap<String, ParkingSpot> parkingSpots;
    private HashMap<String, RentParking> rentHouses;

    // Holds the userID that reports the current position as free
    private String userID;

    // Contains current user data
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_spot);

        // Init app context
        mContext = this;

        // Initialize phase
        init();

        // Read data from database
        readFromDatabase();

        // Setup leave button
        leaveButton();
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
        privateHouses = new HashMap<String, PrivateParking>();
        parkingSpots = new HashMap<String, ParkingSpot>();
        rentHouses = new HashMap<String, RentParking>();

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mParkingSpotDBRef = mFirebaseDatabase.getReference().child("parking_spots");
        mStreetParkingRef = mFirebaseDatabase.getReference().child("street_parking");
        mPrivateParkingRef = mFirebaseDatabase.getReference().child("private_parking");
        mRentedParkingRef = mFirebaseDatabase.getReference().child("rented_parking");
        mUsersRef = mFirebaseDatabase.getReference().child("users").child(mAuth.getCurrentUser().getUid());

        // Init google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Setup callbacks in order to get data from Database
     */
    private void readFromDatabase() {

        // Get current user
        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    // Assign user to user ref
                    user = dataSnapshot.getValue(User.class);
                    userID = dataSnapshot.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Populates streetHouses<String, StreetParking> HashMap
        mStreetParkingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null)
                    streetHouses.put(dataSnapshot.getKey(), streetHouse);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null)
                    streetHouses.put(dataSnapshot.getKey(), streetHouse);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                StreetParking streetHouse = dataSnapshot.getValue(StreetParking.class);
                if (streetHouse != null)
                    streetHouses.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Populates privateHouses<String, PrivateParking> HashMap
        mPrivateParkingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PrivateParking privateHouse = dataSnapshot.getValue(PrivateParking.class);
                if (privateHouse != null)
                    privateHouses.put(dataSnapshot.getKey(), privateHouse);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PrivateParking privateHouse = dataSnapshot.getValue(PrivateParking.class);
                if (privateHouse != null)
                    privateHouses.put(dataSnapshot.getKey(), privateHouse);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                PrivateParking privateHouse = dataSnapshot.getValue(PrivateParking.class);
                if (privateHouse != null)
                    privateHouses.remove(dataSnapshot.getKey());
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
                if (rentHouse != null)
                    rentHouses.put(dataSnapshot.getKey(), rentHouse);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RentParking rentHouse = dataSnapshot.getValue(RentParking.class);
                if (rentHouse != null)
                    rentHouses.put(dataSnapshot.getKey(), rentHouse);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                RentParking rentHouse = dataSnapshot.getValue(RentParking.class);
                if (rentHouse != null)
                    rentHouses.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void leaveButton() {
        mLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // We are not allowing users to continually report spots
                // time frame between two reports: 2 minutes

                // Get elapsed time since lastReportedTimestamp
                long lastReportedTimestamp = user.getLastReportTimestamp();
                final long currentTimestamp = Instant.now().toEpochMilli();
                Date reportedDate = new Date(lastReportedTimestamp);
                Date currentDate = new Date(currentTimestamp);
                long minutesDiff = Utils.getDateDiff(reportedDate, currentDate, TimeUnit.MINUTES);

                // Don't allow user to continually report positions
                if (minutesDiff < 2) {
                    String msg = "You have already reported a parking spot. You can't report so soon again!";
                    Utils.showMessageToUser(mContext, "Attention!", msg);
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

        // TODO: Check first if user is parked in private_parking
        final String parkingHouseId = user.getParkingHouseId();

        if (privateHouses.containsKey(parkingHouseId)) {
            // update parking house
            updateUserData(0);

            // User is reporting spot from private_house
            updateParkingHouse(parkingHouseId, "private_parking", privateHouses.get(parkingHouseId).getOccupied() - 1);

            String msg = "You have just left the Parking House";
            Utils.showMessageToUser(mContext, "Attention", msg);

        } else if (rentHouses.containsKey(parkingHouseId)) {
            // update user data
            updateUserData(0);

            updateRentedHouse(parkingHouseId, "rented_parking", false);

            // updateParkingHouse(parkingHouseId, "rented_parking", rentHouses.get(parkingHouseId).getOccupied() - 1);
            String msg = "You have just left the Rented Spot";
            Utils.showMessageToUser(mContext, "Attention", msg);

        } else if (streetHouses.containsKey(parkingHouseId)) {
            // Update user data
            updateUserData(currentTimestamp);

            // Update parkinghouse data
            updateParkingHouse(parkingHouseId, "street_parking", streetHouses.get(parkingHouseId).getOccupied() - 1);

            // Create new ParkingSpot in database
            createParkingSpot(parkingHouseId, currentTimestamp);

            // Show message to user
            String msg = "You have just reported a free spot at: " + streetHouses.get(parkingHouseId).getAddress();
            Utils.showMessageToUser(mContext, "Attention", msg);
        } else {
            // User is not parked in a parking house, so we promt a
            // popup window with possible roads he is leaving from

            // Popup user with the closest streets
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ReportSpotActivity.this);
            builderSingle.setIcon(R.drawable.icon_info_window);
            builderSingle.setTitle("Choose Street");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ReportSpotActivity.this, android.R.layout.select_dialog_singlechoice);

            // Calculate closest houses
            HashMap<String, StreetParking> closestHouses = getClosestHouses(new LatLng(latit, longtit));

            if (closestHouses.size() == 0) {
                // Show message to user
                String msg = "There isn't any StreetHouse close to you, in order to report a free spot!";
                Utils.showMessageToUser(mContext, "Attention!", msg);
                return;
            }

            // Collection that holds address, parkingHousesIds
            final HashMap<String, String> addressToKey = new HashMap<String, String>();
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
                        public void onClick(DialogInterface dialog, int which) {
                            // Updates user data
                            updateUserData(currentTimestamp);

                            // Report Free Spot
                            updateParkingHouse(addressToKey.get(strName), "street_parking", streetHouses.get(addressToKey.get(strName)).getOccupied() - 1);

                            // Creates new parking spot
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

    /**
     * Updates ParkingHouse occupied data
     * @param parkingHouseId
     * @param typeOfHouse
     * @param newOccupied
     */
    private void updateParkingHouse(String parkingHouseId, String typeOfHouse, int newOccupied) {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(typeOfHouse).child(parkingHouseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put("occupied", newOccupied);
            ref.updateChildren(data);
        }
    }

    /**
     * Updates RentedHouse occupied data
     * @param parkingHouseId
     * @param typeOfHouse
     * @param newOccupied
     */
    private void updateRentedHouse(String parkingHouseId, String typeOfHouse, boolean newOccupied) {

        if (parkingHouseId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(typeOfHouse).child(parkingHouseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put("occupied", newOccupied);
            ref.updateChildren(data);
        }
    }


    /**
     * Updates User data in database
     */
    private void updateUserData(long timestamp) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> data = new HashMap<>();
        if (timestamp != 0)
            data.put("lastReportTimestamp", timestamp);
        data.put("parked", false);
        data.put("parkingHouseId", "0");
        data.put("latit", 0);
        data.put("longtit", 0);
        ref.updateChildren(data);
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
     * Calculates which streetHouses are the closest to the user
     * @param userLocation
     * @return
     */
    private HashMap<String, StreetParking> getClosestHouses(LatLng userLocation) {
        HashMap<String, StreetParking> houses = new HashMap<>();
        double meters;

        for (HashMap.Entry<String, StreetParking> entry : streetHouses.entrySet()) {
            LatLng houseLocation = new LatLng(entry.getValue().getLatit(), entry.getValue().getLongtit());
            meters = SphericalUtil.computeDistanceBetween(userLocation, houseLocation);
            if (meters < 1000) {
                houses.put(entry.getKey(), entry.getValue());
            }
        }

        return houses;
    }


    private void renderMap(float latit, float longtit) {

        // Get user location
        double userLat = user.getLatit();
        double userLong = user.getLongtit();

        if (userLat != 0 && userLong != 0) {
            // Render User on his coordinates
            String address = Utils.getStreetAddress(userLat, userLong, this);
            LatLng coordinates = new LatLng(userLat, userLong);
            mMap.addMarker(new MarkerOptions()
                    .position(coordinates)
                    .title(address)
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_user)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 14));
        } else {
            // Otherwise, get status from gps
            String address = Utils.getStreetAddress(latit, longtit, this);
            LatLng coordinates = new LatLng(latit, longtit);
            mMap.addMarker(new MarkerOptions()
                    .position(coordinates)
                    .title(address)
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_user)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 14));
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.i(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.i(TAG, "Can't find style. Error: ", e);
        }

        // Get user last known location and render it on map
        getCurrentLocation();
    }


    private void getCurrentLocation() {
        // Get current location
        if (!Permissions.Check_FINE_LOCATION(ReportSpotActivity.this)) {
            //if not permission granted so request permission with request code
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
                                renderMap(latit, longtit);
                            } else {
                                renderMap(0, 0);
                            }
                        }
                    });
        }
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
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
