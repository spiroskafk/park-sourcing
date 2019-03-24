package com.spiroskafk.parking.activities.user;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.RentParking;
import com.spiroskafk.parking.utils.Permissions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class RentYourPlace extends AppCompatActivity {

    // Log TAG
    private static final String TAG = RentYourPlace.class.getSimpleName();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRentedSpotDatabaseReference;

    // UI components
    private RadioGroup mRadioGroup;
    private EditText mFromEditText;
    private EditText mUntilEditText;
    private EditText mNOSEditText;
    private Button mRentBtn;

    // Location
    private LatLng location;
    private String locationName;

    // Auto-complete address
    private PlaceAutocompleteFragment autocompleteFragment;

    // LocationClient
    private FusedLocationProviderClient mFusedLocationClient;

    // Rest
    Calendar myCalendar = Calendar.getInstance();

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

        // Init auto-complete address
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Init firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRentedSpotDatabaseReference = mFirebaseDatabase.getReference().child("rented_parking");
        // Init Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init UI
        initUI();

    }

    private void initUI() {
        // Init all UI components here
        mRentBtn = findViewById(R.id.button_rent_place);
        mNOSEditText = findViewById(R.id.edittext_no_spaces);
        mFromEditText = findViewById(R.id.edittext_rent_from);
        mUntilEditText = findViewById(R.id.edittext_rent_until);
        mRadioGroup = findViewById(R.id.radio_group);

    }

    private void setupUIListeners() {

        autoCompleteListener();

        // Setup calendar listeners
        calendarListeners();

        // Setup radio group listener
        radioGroupListener();

        // Setup button listener
        rentBtnListener();

    }

    private void autoCompleteListener() {
        // Setup AutoComplete listener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.i(TAG, "Place: " + place.getName());
                location = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                locationName = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //Log.i(TAG, "An error occurred: " + status);
            }
        });
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
                if (location != null && locationName != null)
                    rentSpot(location.latitude, location.longitude);
                else {
                    Toast.makeText(RentYourPlace.this, "You haven't filled in all the required details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void rentSpot(double latit, double longtit) {
        // Check if all ui components are filled in with details
        String nos = mNOSEditText.getText().toString();
        String fromDate = mFromEditText.getText().toString();
        String untilDate = mUntilEditText.getText().toString();

        // RadioGroup input
        int radioGroupInput = mRadioGroup.getCheckedRadioButtonId();

        if (!nos.isEmpty()
                && !fromDate.isEmpty() && !untilDate.isEmpty() && location != null && locationName != null
                && radioGroupInput != -1) {
            // All input fields are filled with details
            // Create new entry in database


            // First, getUser ID
            FirebaseUser user = mAuth.getCurrentUser();
            String userID = user.getUid();

            // Generate UID
            String id = UUID.randomUUID().toString();

            String type = "";

            switch (radioGroupInput) {
                case 1:
                    type = "Normal spot";
                    break;

                case 2:
                    type = "Big vehicle";
                    break;

                case 3:
                    type = "AMEA";
                    break;
            }

            // Create RentData
            RentParking rentParking = new RentParking(locationName, userID, id, fromDate, untilDate, nos, type, latit, longtit, false);


            // Add to firebase
            mRentedSpotDatabaseReference.push().setValue(rentParking);
            Toast.makeText(RentYourPlace.this, "You have successfully added you spot for rent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RentYourPlace.this, "You haven't filled in all the required details", Toast.LENGTH_SHORT).show();
        }
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

}
