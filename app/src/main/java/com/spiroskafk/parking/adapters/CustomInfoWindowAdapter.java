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

        // Based on Marker's Tag (StreetParking, PrivateParking, etc)
        // load the appropriate layout
        if (marker.getSnippet().equals("parking_house")) {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.new_private_infowindow, null);

            TextView mAddressTv = view.findViewById(R.id.address_parking_house_tv);
            TextView mEntrance = view.findViewById(R.id.entrance_parking_house_tv);
            TextView mSpaces = view.findViewById(R.id.spaces_parking_house_tv);
            TextView mHourlyChargeTv = view.findViewById(R.id.charge_parking_house_tv);
            TextView mDistanceTv = view.findViewById(R.id.distance_parking_house_tv);
            TextView mOfferTv = view.findViewById(R.id.tv_offer);
            TextView mTimeFromTv = view.findViewById(R.id.tv_time_from);
            TextView mTimeUntilTv = view.findViewById(R.id.tv_time_until);

            InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

            mAddressTv.setText(infoWindowData.getAddress());
            mSpaces.setText(String.valueOf(infoWindowData.getCapacity() - infoWindowData.getOccupied()));
            mEntrance.setText(String.valueOf(infoWindowData.getEntrance()));
            mHourlyChargeTv.setText(String.valueOf(infoWindowData.getHourlyCharge()));
            mOfferTv.setText(String.valueOf(infoWindowData.getOffer()));
            mTimeFromTv.setText(String.valueOf(infoWindowData.getTimeFrom()));
            mTimeUntilTv.setText(String.valueOf(infoWindowData.getTimeUntil()));

            return view;

        } else if (marker.getSnippet().equals("street_parking")){
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.street_parking_infowindow, null);
            TextView mAddressTv = view.findViewById(R.id.address_street_parking_tv);
            TextView mSpacesTv = view.findViewById(R.id.spaces_street_parking_tv);
            TextView mDistanceTv = view.findViewById(R.id.distance_street_parking_tv);

            InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

            mAddressTv.setText(infoWindowData.getAddress());
            mSpacesTv.setText(infoWindowData.getSpaces());
            mDistanceTv.setText(infoWindowData.getDistance());

            return view;
        } else if (marker.getSnippet().equals("space_to_rent")) {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.space_to_rent_infowindow, null);

            TextView mAddressTv = view.findViewById(R.id.address_to_rent_tv);
            TextView mSpacesTv = view.findViewById(R.id.spaces_to_rent_tv);
            TextView mDistanceTv = view.findViewById(R.id.distance_to_rent_tv);
            TextView mFromTv = view.findViewById(R.id.from_to_rent_tv);
            TextView mUntilTv = view.findViewById(R.id.until_to_rent_tv);
            TextView mTypeTv = view.findViewById(R.id.type_to_rent_tv);

            InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

            mAddressTv.setText(infoWindowData.getAddress());
            mSpacesTv.setText(infoWindowData.getSpaces());
            mDistanceTv.setText(infoWindowData.getDistance());
            mFromTv.setText(infoWindowData.getFrom());
            mUntilTv.setText(infoWindowData.getUtil());
            mTypeTv.setText(infoWindowData.getType());

            return view;

        } else if (marker.getSnippet().equals("user_info")) {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.user_info_window,  null);
//            TextView mAddressTv = view.findViewById(R.id.address_user_position_tv);

            InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

//            mAddressTv.setText(infoWindowData.getAddress());

            return view;
        }

        return null;
    }
}