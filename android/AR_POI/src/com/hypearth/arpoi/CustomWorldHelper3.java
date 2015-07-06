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

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CustomWorldHelper3 {
    public static final String LIST_TYPE_EVENT = "event";
    public static final int LIST_TYPE_EVENT_CODE = LIST_TYPE_EVENT.hashCode();

    public static final String LIST_TYPE_ORGANISATION = "organisation";
    public static final int LIST_TYPE_ORGANISATION_CODE = LIST_TYPE_ORGANISATION.hashCode();

    public static final String LIST_TYPE_PLACE = "place";
    public static final int LIST_TYPE_PLACE_CODE = LIST_TYPE_PLACE.hashCode();

    public static final String[] HISTORY_SA_LIST_TYPES = {LIST_TYPE_EVENT, LIST_TYPE_ORGANISATION, LIST_TYPE_PLACE};

    public static final Map<GeoObject, String> OBJECT_DESCRIPTION_MAP = new HashMap<>();
    // Tag used to cancel the JSON HTTP request
    static final String tag_json_obj = "json_obj_req/";
    public static World sharedWorld;

    public static World generateObjects(Context context) {
        Log.i(CustomWorldHelper3.class.getName(), "generateObjects");

        if (sharedWorld != null) {
            return sharedWorld;
        }
        sharedWorld = new World(context);

        // The user can set the default bitmap. This is useful if you are
        // loading images form Internet and the connection get lost
        sharedWorld.setDefaultImage(R.drawable.beyondar_default_unknow_icon);

        // sharedWorld.setGeoPosition(41.90533734214473d, 2.565848038959814d);

        Log.i(CustomWorldHelper3.class.getName(), "generateObjects() attempting to get History SA places");
        final String urlParent = "http://data.history.sa.gov.au/sahistoryhub/";
        final String urlEvent = "event";
        final String urlOrganisation = "organisation";
        final String urlPlace = "place";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading History SA data...");
        pDialog.show();

        // Object Id needs to be an atomic sequence of Long:
        final AtomicLong objectId = new AtomicLong(2000L);

        for (final String url : HISTORY_SA_LIST_TYPES) {

            final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlParent + url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(CustomWorldHelper3.class.getName(), response.toString());
                        pDialog.hide();

                        Log.i(CustomWorldHelper3.class.getName(), "Got History SA " + url + "s...");
                        try {
                            final JSONArray features = response.getJSONArray("features");
                            if (features != null) {
                                Log.i(CustomWorldHelper3.class.getName(), "Got History SA " + url + " Features, count=" + features.length());
                                for (int i = 0; i < features.length(); i++) {
                                    final JSONObject feature = features.getJSONObject(i);
                                    final JSONObject geometry = feature.getJSONObject("geometry");
                                    final JSONArray coordinates = geometry.getJSONArray("coordinates");
                                    final double lng = coordinates.getDouble(0);
                                    final double lat = coordinates.getDouble(1);
                                    final JSONObject properties = feature.getJSONObject("properties");
                                    final String title = properties.getString("TITLE");
                                    final String description = properties.getString("DESCRIPTION");

                                    final GeoObject go1 = new GeoObject(objectId.incrementAndGet());
                                    go1.setGeoPosition(lat, lng);
                                    go1.setImageUri("assets://historysalogo-" + url + ".png");
                                    go1.setName(title);
                                    OBJECT_DESCRIPTION_MAP.put(go1, description);
                                    // Add the GeoObject to the world
                                    // color-code Places/Events/Organisations/photos etc
                                    sharedWorld.addBeyondarObject(go1, url.hashCode());
                                }
                            }

                        } catch (JSONException e) {
                            Log.e(CustomWorldHelper3.class.getName(), "Problem fetching GEOJSON \" + url + \" from HistorySA", e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(CustomWorldHelper3.class.getName(), "Error: " + error.getMessage());
                        pDialog.hide();
                    }
                }) {
            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj + url);
        }


        return sharedWorld;
    }

}
