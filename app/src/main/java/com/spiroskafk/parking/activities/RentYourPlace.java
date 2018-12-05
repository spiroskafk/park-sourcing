package com.spiroskafk.parking.activities;

import android.app.DatePickerDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.RentData;
import com.spiroskafk.parking.utils.Permissions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class RentYourPlace extends AppCompatActivity {

    // Log TAG
    public final String TAG = "RentYourPlace";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRentedSpotDatabaseReference;

    // UI components
    private RadioGroup mRadioGroup;
    private EditText mFromEditText;
    private EditText mUntilEditText;
    private EditText mAddressEditText;
    private EditText mCityEditText;
    private EditText mNOSEditText;
    private EditText mCommentsEditText;
    private Button mRentBtn;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // Rest
    Calendar myCalendar = Calendar.getInstance();
    private static float latit;
    private static float longtit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_your_place);

        // Initialize phase
        init();

        // Setup UI listeners
        setupUIListeners();
    }

    private void init() {
        // Init - Setup toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRentedSpotDatabaseReference = mFirebaseDatabase.getReference().child("rented_spots");
        // Init Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init UI
        initUI();

    }

    private void initUI() {
        // Init all UI components here
        mRentBtn = findViewById(R.id.rent_btn);
        mAddressEditText = findViewById(R.id.address_edit_text);
        mCityEditText = findViewById(R.id.city_edit_text);
        mNOSEditText = findViewById(R.id.nos_edit_text);
        mCommentsEditText = findViewById(R.id.comments_edit_text);
        mFromEditText = findViewById(R.id.rent_from);
        mUntilEditText = findViewById(R.id.rent_until);
        mRadioGroup = findViewById(R.id.radio_group);

    }

    private void setupUIListeners() {

        // Setup calendar listeners
        calendarListeners();

        // Setup radio group listener
        radioGroupListener();

        // Setup button listener
        rentBtnListener();

    }

    /** UI Elements Click Listeners */
    private void radioGroupListener() {
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TAG, "Item clicked: " + checkedId);
            }
        });
    }

    private void rentBtnListener() {
        mRentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if all ui components are filled in with details
                String address = mAddressEditText.getText().toString();
                String city = mCityEditText.getText().toString();
                String nos = mNOSEditText.getText().toString();
                String fromDate = mFromEditText.getText().toString();
                String untilDate = mUntilEditText.getText().toString();
                String comments = mCommentsEditText.getText().toString();

                // RadioGroup input
                int radioGroupInput = mRadioGroup.getCheckedRadioButtonId();

                if (!address.isEmpty() && !city.isEmpty() && !nos.isEmpty()
                        && !fromDate.isEmpty() && !untilDate.isEmpty() && !comments.isEmpty()
                        && radioGroupInput != -1) {
                    // All input fields are filled with details
                    // Create new entry in database

                    // Get location
                    getCurrentLocation();

                    // First, getUser ID
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userID = user.getUid();

                    // Generate UID
                    String id = UUID.randomUUID().toString();

                    // Create RentData
                    RentData rentData = new RentData(address, userID, id, city, fromDate, untilDate, nos, comments, String.valueOf(radioGroupInput), latit, longtit);


                    // Add to firebase
                    mRentedSpotDatabaseReference.push().setValue(rentData);
                    Toast.makeText(RentYourPlace.this, "Προστέθηκε νέα εγγραφή στη βάση", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void updateDateEditText(EditText et) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        et.setText(sdf.format(myCalendar.getTime()));
    }


    private void calendarListeners() {
        final DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateEditText(mFromEditText);
            }

        };
        final DatePickerDialog.OnDateSetListener untilDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateEditText(mUntilEditText);
            }

        };
        mFromEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(RentYourPlace.this, fromDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mUntilEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(RentYourPlace.this, untilDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getCurrentLocation() {
        // Get current location
        if (!Permissions.Check_FINE_LOCATION(RentYourPlace.this)) {
            //if not permisson granted so request permisson with request code
            Permissions.Request_FINE_LOCATION(RentYourPlace.this, 22);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latit = (float) location.getLatitude();
                                longtit = (float) location.getLongitude();
                            } else {

                            }
                        }
                    });
        }
    }


}
