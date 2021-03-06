package com.spiroskafk.parking.activities.company;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.User;

import java.util.HashMap;

public class CompanyMaps extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    private static final String TAG = CompanyMaps.class.getSimpleName();

    // Google map
    private GoogleMap mMap;

    private HashMap<String, PrivateParking> privateHouses;
    private String houseId;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_maps);

        init();

        // Get data from CompanyDashboard
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.i(TAG, "Bundle extras");
            privateHouses = (HashMap<String, PrivateParking>) extras.getSerializable("houses");
            houseId = extras.getString("houseId");
            user = (User) extras.getSerializable("user");
        }
    }

    private void renderMap() {
        Log.i(TAG, "renderMap()");
        LatLng coordinates = new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit());
        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit()))
                .title(privateHouses.get(houseId).getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(privateHouses.get(houseId).getLatit(), privateHouses.get(houseId).getLongtit()), 14));

    }


    private void init() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Init Google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady()");

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

        renderMap();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
