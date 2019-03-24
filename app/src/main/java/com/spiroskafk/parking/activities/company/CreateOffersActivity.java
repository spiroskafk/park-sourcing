package com.spiroskafk.parking.activities.company;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.Offer;
import com.spiroskafk.parking.model.PrivateParking;
import com.spiroskafk.parking.model.User;
import com.spiroskafk.parking.utils.TimePickerUniversal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CreateOffersActivity extends AppCompatActivity {

    // Log TAG
    private static final String TAG = CreateOffersActivity.class.getSimpleName();

    // UI components
    private RadioGroup mRadioGroup;
    private EditText mDateFromEditText;
    private EditText mDateUntilEditText;
    private EditText mTimeFromditText;
    private EditText mTimeUntilEditText;
    private Button mCreateOffersBtn;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mOffersDatabaseRef;

    private HashMap<String, PrivateParking> privateHouses;
    private String houseId;
    private User user;

    // Rest
    Calendar myCalendar = Calendar.getInstance();

    private EditText text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offers);

        // Get data from CompanyDashboard
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.i(TAG, "Bundle extras");
            privateHouses = (HashMap<String, PrivateParking>) extras.getSerializable("houses");
            houseId = extras.getString("houseId");
            user = (User) extras.getSerializable("user");
        }

        init();

        setupUIListeners();
    }

    private void init() {

        // Init - Setup toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Init UI
        mDateFromEditText = findViewById(R.id.edittext_date_from);
        mDateUntilEditText = findViewById(R.id.edittext_date_until);
        mTimeFromditText = findViewById(R.id.edittext_time_from);
        mTimeUntilEditText = findViewById(R.id.edittext_time_until);
        mRadioGroup = findViewById(R.id.radio_group_offers);
        mCreateOffersBtn = findViewById(R.id.button_create_offer);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mOffersDatabaseRef = mFirebaseDatabase.getReference().child("offers");

    }

    private void setupUIListeners() {

        // Setup calendar listeners
        calendarListeners();

        // Setup Time listeners
        timeListeners();

        // Setup radio group listener
        radioGroupListener();

        createOffers();
    }

    private void createOffers() {
        mCreateOffersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromDate = mDateFromEditText.getText().toString();
                String untilDate = mDateUntilEditText.getText().toString();
                String fromTime = mTimeFromditText.getText().toString();
                String untilTime = mTimeUntilEditText.getText().toString();

                // RadioGroup input
                int radioGroupInput = mRadioGroup.getCheckedRadioButtonId();

                if (fromDate.contains("/") && untilDate.contains("/") &&
                fromTime.contains(":") && untilTime.contains(":") && radioGroupInput != -1) {

                    // All input fields are filled with details
                    // Create new entry in database

                    String type = "";

                    switch (radioGroupInput) {
                        case 1:
                            type = "-10% For 2Hours";
                            break;

                        case 2:
                            type = "-20% For 5Hours";
                            break;

                        case 3:
                            type = "-40% For 10Hours";

                    }

                    // Create Offer
                    Offer offer = new Offer(houseId, type, fromTime, untilTime);
                    mOffersDatabaseRef.push().setValue(offer);
                    Toast.makeText(CreateOffersActivity.this, "You have successfully added a new offer", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateOffersActivity.this, "You haven't filled in all the required details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void calendarListeners() {
        final DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateEditText(mDateFromEditText);
            }

        };
        final DatePickerDialog.OnDateSetListener untilDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateEditText(mDateUntilEditText);
            }

        };
        mDateFromEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateOffersActivity.this, fromDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mDateUntilEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateOffersActivity.this, untilDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateDateEditText(EditText et) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        et.setText(sdf.format(myCalendar.getTime()));
    }

    private void timeListeners() {
        new TimePickerUniversal(mTimeFromditText ,true);
        new TimePickerUniversal(mTimeUntilEditText ,true);
    }

    private void radioGroupListener() {
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TAG, "Item clicked: " + checkedId);
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }




}
