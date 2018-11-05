package com.spiroskafk.parking.utils;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import static com.firebase.ui.auth.AuthUI.TAG;

public class Utils {
    public static String getStreetAddress(double latitude, double longitude, Activity context) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.i(TAG, "Address = " + strReturnedAddress.toString());
            } else {
                Log.i(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Cannot get Address!");
        }
        return strAdd;
    }


}
