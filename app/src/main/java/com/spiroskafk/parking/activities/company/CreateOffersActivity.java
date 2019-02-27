package com.spiroskafk.parking.activities.company;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.spiroskafk.parking.R;
import com.spiroskafk.parking.adapters.TimePickerFragment;
import com.spiroskafk.parking.utils.TimePickerUniversal;

public class CreateOffersActivity extends AppCompatActivity {

    private EditText text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offers);
        text = findViewById(R.id.pickme);
        new TimePickerUniversal(text ,true);
    }


}
