package com.spiroskafk.parking.activities.company;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.User;

import java.util.HashMap;

public class InventoryActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = InventoryActivity.class.getSimpleName();

    // UI elements
    private EditText mCapacity;
    private EditText mOccupied;
    private EditText mHourlyCharge;
    private EditText mEntranceFee;
    private TextView mCapacityTv;
    private TextView mOccupiedTv;
    private TextView mEntraceFeeTv;
    private TextView mHourlyChargeTv;
    private Button mUpdateBtn;

    // Firebase vars
    private FirebaseAuth mAuth;

    private User user;
    private HashMap<String, PrivateParking> privateHouses;
    private String houseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        init();

        // Get data from CompanyDashboard
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.i(TAG, "Bundle extras");
            privateHouses = (HashMap<String, PrivateParking>) extras.getSerializable("houses");
            houseId = extras.getString("houseId");
            user = (User) extras.getSerializable("user");
        }

        setupListeners();
    }

    private void init() {
        // Init - Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mCapacity = findViewById(R.id.edittext_capacity);
        mOccupied = findViewById(R.id.edittext_occupied);
        mHourlyCharge = findViewById(R.id.edittext_hourly_charge);
        mEntranceFee = findViewById(R.id.edittext_entrance_fee);

        mCapacityTv = findViewById(R.id.textview_capacity);
        mOccupiedTv = findViewById(R.id.textview_occupied);
        mHourlyChargeTv = findViewById(R.id.textview_hourly_charge);
        mEntraceFeeTv = findViewById(R.id.textview_entrance_fee);

        mUpdateBtn = findViewById(R.id.button_update);

        mAuth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {

        // Setup listeners
        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input text
                String occupied = mOccupied.getText().toString();
                String capacity = mCapacity.getText().toString();
                String charge = mHourlyCharge.getText().toString();
                String entrance = mEntranceFee.getText().toString();

                Log.i(TAG, "onClick");
                Log.i(TAG, "Occupied : " + occupied);
                Log.i(TAG, "Capacitiy: " + capacity);

                if (!occupied.isEmpty()) {
                    mOccupiedTv.setText(occupied);
                    updateDatabase("occupied", occupied);

                }
                if (!capacity.isEmpty()) {
                    mCapacityTv.setText(capacity);
                    updateDatabase("capacity", capacity);
                }

                if (!charge.isEmpty()) {
                    mHourlyChargeTv.setText(charge + "€");
                    updateDatabase("hourlyCharge", charge);
                }

                if (!entrance.isEmpty()) {
                    mEntraceFeeTv.setText(entrance + "€");
                    updateDatabase("entrance", entrance);
                }

            }
        });

        fillTextViewsFromDB();
    }

    private void fillTextViewsFromDB() {
        if (houseId == null || user == null || privateHouses == null) return;
        Log.i(TAG, "fillTextViewsFromDB()");

        DatabaseReference parkingHouseRef = FirebaseDatabase.getInstance().getReference().child("private_parking").child(houseId);
        parkingHouseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "DataChanged");
                PrivateParking parking = dataSnapshot.getValue(PrivateParking.class);
                if (parking == null) return;

                mCapacityTv.setText(String.valueOf(parking.getCapacity()));
                mOccupiedTv.setText(String.valueOf(parking.getOccupied()));
                mHourlyChargeTv.setText(parking.getHourlyCharge() + "€");
                mEntraceFeeTv.setText(parking.getEntrance() + "€");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateDatabase(String key, String value) {
        if (houseId == null) return;
        int capacity;
        int occupied;

        Log.i(TAG, "Key = " + key);
        Log.i(TAG, "Value = " + value);

        if (key.equals("capacity")) {
            capacity = Integer.parseInt(value);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("private_parking").child(houseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put(key, capacity);
            ref.updateChildren(data);
            Log.i(TAG, "Capacity in update: " + capacity);
        } else if (key.equals("occupied")) {
            occupied = Integer.parseInt(value);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("private_parking").child(houseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put(key, occupied);
            ref.updateChildren(data);
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("private_parking").child(houseId);
            HashMap<String, Object> data = new HashMap<>();
            data.put(key, value);
            ref.updateChildren(data);
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
