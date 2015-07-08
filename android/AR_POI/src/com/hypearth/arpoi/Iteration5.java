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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

import java.util.ArrayList;

public class Iteration5 extends FragmentActivity implements OnSeekBarChangeListener, View.OnClickListener, LocationListener, OnClickBeyondarObjectListener {

    private BeyondarFragmentSupport mBeyondarFragment;
    private RadarView mRadarView;
    private RadarWorldPlugin mRadarPlugin;
    private World mWorld;
    private LocationManager mLocationManager;

    private SeekBar mSeekBarMaxDistance;
    private TextView mTextviewMaxDistance;
    private Button mShowMap;

    private BeyondarObject selectedObject = null;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        loadViewFromXML();

        // maximum distance of objects from user to consider rendering (metres)
        mBeyondarFragment.setMaxDistanceToRender(2_000);

        // Create the Radar plugin
        mRadarPlugin = new RadarWorldPlugin(this);
        // set the radar view in to our radar plugin
        mRadarPlugin.setRadarView(mRadarView);
        // Set how far (in meters) we want to display in the view
        mRadarPlugin.setMaxDistance(4_000);

        // We can customize the color of the items on the Radar view
        mRadarPlugin.setListColor(CustomWorldHelper5.LIST_TYPE_EVENT_CODE, Color.RED);
        mRadarPlugin.setListColor(CustomWorldHelper5.LIST_TYPE_ORGANISATION_CODE, Color.GREEN);
        mRadarPlugin.setListColor(CustomWorldHelper5.LIST_TYPE_PLACE_CODE, Color.YELLOW);
        mRadarPlugin.setListColor(CustomWorldHelper5.LIST_TYPE_THING_CODE, Color.BLUE);
        mRadarPlugin.setListColor(CustomWorldHelper5.LIST_TYPE_NEWS_CODE, Color.MAGENTA);
        // and also the size of the dots on the Radar view
        // mRadarPlugin.setListDotRadius(CustomWorldHelper4.LIST_TYPE_EXAMPLE_1, 3);

        // We create the world and fill it ...
        mWorld = CustomWorldHelper5.generateObjects(this);

        // add the plugin
        mWorld.addPlugin(mRadarPlugin);

        // .. and send it to the fragment
        mBeyondarFragment.setWorld(mWorld);

        // Radar range slider
        mSeekBarMaxDistance.setOnSeekBarChangeListener(this);
        mSeekBarMaxDistance.setMax(10_000);
        mSeekBarMaxDistance.setProgress(4_000);

        // Lets add the user position to the map
        GeoObject user = new GeoObject(1_000L);
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

        // set "click" listener for the geoObjects
        mBeyondarFragment.setOnClickBeyondarObjectListener(this);

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
        Log.i(Iteration5.class.getName(), "loadViewFromXML() start");

        setContentView(R.layout.simple_camera_with_radar);

        mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
                R.id.beyondarFragment);

        mShowMap = (Button) findViewById(R.id.showMapButton);
        if (mShowMap != null)
            mShowMap.setOnClickListener(this);

        mTextviewMaxDistance = (TextView) findViewById(R.id.textMaxDistance);

        mSeekBarMaxDistance = (SeekBar) findViewById(R.id.seekBarMaxDistance);

        mRadarView = (RadarView) findViewById(R.id.radarView);

        Log.i(Iteration5.class.getName(), "loadViewFromXML() done");
    }

    @Override
    public void onClick(View v) {
        if (v == mShowMap) {
            Intent intent = new Intent(this, GoogleMapActivity5.class);
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

    @Override
    public void onClickBeyondarObject(final ArrayList<BeyondarObject> beyondarObjects) {
        if (beyondarObjects.size() > 0) {
            BeyondarObject firstObj = beyondarObjects.get(0);
            if (firstObj.equals(selectedObject)) {
                // selected object has been clicked on again
                final String moreInfoUrl = CustomWorldHelper5.OBJECT_INFO_URL_MAP.get(firstObj);
                // launch this URL as intent
                Uri webpage = Uri.parse(moreInfoUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            } else {
                selectedObject = firstObj;
                final long firstObjId = firstObj.getId();
                final String descriptionText;

                // Lookup description saved in the CustomWorldHelper dictionary:
                final String lookupDescription = CustomWorldHelper5.OBJECT_DESCRIPTION_MAP.get(firstObj);
                if (lookupDescription == null) {
                    descriptionText = "";
                } else {
                    descriptionText = ": " + lookupDescription;
                }
                Toast.makeText(this, firstObj.getName() + descriptionText,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
