/*
 * Copyright (C) 2014 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hypearth.arpoi;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.beyondar.android.plugin.googlemap.GoogleMapWorldPlugin;
import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class GoogleMapActivity5 extends FragmentActivity implements OnMarkerClickListener, LocationListener {

    private GoogleMap mMap;
    private GoogleMapWorldPlugin mGoogleMapPlugin;
    private World mWorld;
    private Location lastLocation = null;
    private GeoObject selectedGeoObject = null;

    // Cluster manager for markers that would otherwise be overlapping in the view
    private ClusterManager<MyClusterItem> mClusterManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_google);

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap == null) {
            return;
        }

        // Initialize the manager with the context and the map.
        mClusterManager = new ClusterManager<>(this, mMap);
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        // We create the world and fill the world
        mWorld = CustomWorldHelper5.generateObjects(this);

        // As we want to use GoogleMaps, we are going to create the plugin and
        // attach it to the World
        mGoogleMapPlugin = new GoogleMapWorldPlugin(this);
        // Then we need to set the map in to the GoogleMapPlugin
        mGoogleMapPlugin.setGoogleMap(mMap);
        // Now that we have the plugin created let's add it to our world.
        // NOTE: It is better to load the plugins before start adding object in to the world.
        mWorld.addPlugin(mGoogleMapPlugin);

        mMap.setOnMarkerClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mGoogleMapPlugin.getLatLng(), 15));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);

        // add all markers to the Cluster Manager
        // XXX note this will not work if markers have not yet been loaded.
        // TODO add a callback for when a new item is added to the map, so it can also be added to the ClusterManager
        for (final BeyondarObjectList objectList :  mWorld.getBeyondarObjectLists()) {
            for (final BeyondarObject beyondarObject : objectList) {
                if (beyondarObject instanceof GeoObject) {
                    final GeoObject geoObject = (GeoObject)beyondarObject;
                    mClusterManager.addItem(new MyClusterItem(geoObject.getLatitude(), geoObject.getLongitude()));
                }
            }
        }

        // Lets add the user position
        GeoObject user = new GeoObject(1_000L);
        user.setGeoPosition(mWorld.getLatitude(), mWorld.getLongitude());
        user.setImageResource(R.drawable.flag);
        user.setName("User position");
        mWorld.addBeyondarObject(user);

        BeyondarLocationManager.addWorldLocationUpdate(mWorld);
        BeyondarLocationManager.addGeoObjectLocationUpdate(user);
        BeyondarLocationManager.addLocationListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // To get the GeoObject that owns the marker we use the following
        // method:
        GeoObject geoObject = mGoogleMapPlugin.getGeoObjectOwner(marker);
        if (geoObject != null) {
            if (geoObject.equals(selectedGeoObject)) {
                // selected marker was clicked again
                String moreInfoUrl = CustomWorldHelper5.OBJECT_INFO_URL_MAP.get(geoObject);
                if (moreInfoUrl != null) {
                    // launch this URL as intent
                    Uri webpage = Uri.parse(moreInfoUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            } else {
                // this marker is now the selected one
                selectedGeoObject = geoObject;
                String text = geoObject.getName();
                String description = CustomWorldHelper5.OBJECT_DESCRIPTION_MAP.get(geoObject);
                if (description != null) text = description;
                Toast.makeText(this,
                        text,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            selectedGeoObject = null;
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mWorld != null) {
            mWorld.setLocation(location);
            if (lastLocation == null || lastLocation.distanceTo(location) > 1000) {
                // only move the map if the movement is greater than 1000 metres
                LatLng userLocation = new LatLng(mWorld.getLatitude(), mWorld.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                // mMap.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);
            }
        }
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // When the activity is resumed it is time to enable the
        // BeyondarLocationManager
        BeyondarLocationManager.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // To avoid unnecessary battery usage disable BeyondarLocationManager
        // when the activity goes on pause.
        BeyondarLocationManager.disable();
    }

}
