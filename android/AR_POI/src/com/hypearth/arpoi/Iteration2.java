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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

public class Iteration2 extends FragmentActivity implements OnSeekBarChangeListener, View.OnClickListener, LocationListener {

    private BeyondarFragmentSupport mBeyondarFragment;
    private RadarView mRadarView;
    private RadarWorldPlugin mRadarPlugin;
    private World mWorld;
    private LocationManager mLocationManager;

    private SeekBar mSeekBarMaxDistance;
    private TextView mTextviewMaxDistance;
    private Button mShowMap;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        loadViewFromXML();

        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        mTextviewMaxDistance = (TextView) findViewById(R.id.textMaxDistance);
        mSeekBarMaxDistance = (SeekBar) findViewById(R.id.seekBarMaxDistance);
        mRadarView = (RadarView) findViewById(R.id.radarView);

        // Create the Radar plugin
        mRadarPlugin = new RadarWorldPlugin(this);
        // set the radar view in to our radar plugin
        mRadarPlugin.setRadarView(mRadarView);
        // Set how far (in meters) we want to display in the view
        mRadarPlugin.setMaxDistance(100);

        // We can customize the color of the items on the Radar view
        mRadarPlugin.setListColor(CustomWorldHelper.LIST_TYPE_EXAMPLE_1, Color.RED);
        // and also the size of the dots on the Radar view
        mRadarPlugin.setListDotRadius(CustomWorldHelper.LIST_TYPE_EXAMPLE_1, 3);

        // We create the world and fill it ...
        mWorld = CustomWorldHelper.generateObjects(this);

        // .. and send it to the fragment
        mBeyondarFragment.setWorld(mWorld);

        // add the plugin
        mWorld.addPlugin(mRadarPlugin);

        // Radar range slider
        mSeekBarMaxDistance.setOnSeekBarChangeListener(this);
        mSeekBarMaxDistance.setMax(10_000);
        mSeekBarMaxDistance.setProgress(4000);

        // Lets add the user position to the map
        GeoObject user = new GeoObject(1000l);
        user.setGeoPosition(mWorld.getLatitude(), mWorld.getLongitude());
        user.setImageResource(R.drawable.flag);
        user.setName("User position");
        mWorld.addBeyondarObject(user);

        BeyondarLocationManager.addWorldLocationUpdate(mWorld);
        BeyondarLocationManager.addGeoObjectLocationUpdate(user);
        BeyondarLocationManager.addLocationListener(this);

        // We need to set the LocationManager to the BeyondarLocationManager.
        BeyondarLocationManager
                .setLocationManager(mLocationManager);

        // Filter Phone Motion Sensor to damp shakey movements
        LowPassFilter.ALPHA = 0.015f;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mRadarPlugin == null)
            return;
        if (seekBar == mSeekBarMaxDistance) {
            // float value = ((float) progress/(float) 10000);
            mTextviewMaxDistance.setText("Radar range: " + progress + "m");
            mRadarPlugin.setMaxDistance(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void loadViewFromXML() {
        Log.i(Iteration2.class.getName(), "loadViewFromXML() start");

        setContentView(R.layout.simple_camera_with_radar);

        mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
                R.id.beyondarFragment);

        mShowMap = (Button) findViewById(R.id.showMapButton);
        if (mShowMap != null)
            mShowMap.setOnClickListener(this);

        Log.i(Iteration2.class.getName(), "loadViewFromXML() done");
    }

    @Override
    public void onClick(View v) {
        if (v == mShowMap) {
            Intent intent = new Intent(this, GoogleMapActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mWorld != null) {
            mWorld.setLocation(location);
        }
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
