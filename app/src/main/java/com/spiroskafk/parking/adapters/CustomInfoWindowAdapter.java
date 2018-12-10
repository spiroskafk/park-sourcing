package com.spiroskafk.parking.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.InfoWindowData;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    // Log TAG
    private static final String TAG = CustomInfoWindowAdapter.class.getSimpleName();

    public CustomInfoWindowAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        Log.i(TAG, "Marker: " + marker.getSnippet());
        if (marker.getSnippet().equals("Private")) {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.private_parking_info_window, null);

            TextView mAddressTv = view.findViewById(R.id.street_private_tv);
            TextView mCapacityTv = view.findViewById(R.id.capacity_tv);
            TextView mOccupiedTv = view.findViewById(R.id.occupied_tv);
            TextView mHourlyChargeTv = view.findViewById(R.id.hourly_charge_tv);
            TextView mDistanceTv = view.findViewById(R.id.distance_private_tv);

            InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

            mAddressTv.setText(infoWindowData.getAddress());
            mCapacityTv.setText(String.valueOf(infoWindowData.getCapacity()));
            mOccupiedTv.setText(String.valueOf(infoWindowData.getOccupied()));
            mHourlyChargeTv.setText(String.valueOf(infoWindowData.getHourlyCharge()));
            mDistanceTv.setText(String.valueOf(infoWindowData.getDistance()));

            return view;

        } else {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_info_window, null);
            TextView mAddressTv = view.findViewById(R.id.street_tv);
            TextView mSpacesTv = view.findViewById(R.id.spaces_tv);
            TextView mDistanceTv = view.findViewById(R.id.distance_tv);
            TextView mTitleTv = view.findViewById(R.id.info_window_title);

            InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

            mTitleTv.setText(infoWindowData.getTitle());
            mAddressTv.setText(infoWindowData.getAddress());
            mSpacesTv.setText(infoWindowData.getSpaces());
            mDistanceTv.setText(infoWindowData.getDistance());

            return view;
        }
    }
}