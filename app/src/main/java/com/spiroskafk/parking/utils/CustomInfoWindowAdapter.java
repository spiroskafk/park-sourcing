package com.spiroskafk.parking.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.spiroskafk.parking.R;
import com.spiroskafk.parking.model.InfoWindowData;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_info_window, null);

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