package com.spiroskafk.parking.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String userType;

    private static final String TAG = Utils.class.getSimpleName();


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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }


    /**
     * Creates a popup message to user
     * @param context
     * @param message
     */
    public static void showMessageToUser(Context context, String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }

                });

        builder.show();
    }
}
