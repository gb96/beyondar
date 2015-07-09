package com.hypearth.arpoi;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by greg on 9/07/2015.
 */
public class MyClusterItem implements ClusterItem {
    private final LatLng mPosition;

    public MyClusterItem(final double lat, final double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}