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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CustomWorldHelper5 {
    // ABC News Local Photo stories.  7.6 MB JSON Format. See http://data.gov.au/dataset/abc-local-online-photo-stories-2009-2014
    public static final String ABC_ONLINE_URL = "http://data.gov.au/dataset/3fd356c6-0ad4-453e-82e9-03af582024c3/resource/3182591a-085a-465b-b8e5-6bfd934137f1/download/Localphotostories2009-2014-JSON.json";

    public static final String LIST_TYPE_EVENT = "event";
    public static final int LIST_TYPE_EVENT_CODE = 0;

    public static final String LIST_TYPE_ORGANISATION = "organisation";
    public static final int LIST_TYPE_ORGANISATION_CODE = 1;

    public static final String LIST_TYPE_PLACE = "place";
    public static final int LIST_TYPE_PLACE_CODE = 2;

    public static final String LIST_TYPE_THING = "thing";
    public static final int LIST_TYPE_THING_CODE = 3;

    // ABC Online News
    public static final String LIST_TYPE_NEWS = "news";
    public static final int LIST_TYPE_NEWS_CODE = 4;

    public static final String[] HISTORY_SA_LIST_TYPES = {
            LIST_TYPE_EVENT,
            LIST_TYPE_ORGANISATION,
            LIST_TYPE_PLACE,
            LIST_TYPE_THING,
    };

    public static final String[] ABC_ONLINE_LIST_TYPES = {
            LIST_TYPE_NEWS,
    };

    public static final Map<GeoObject, String> OBJECT_DESCRIPTION_MAP = new ConcurrentHashMap<>();
    public static final Map<GeoObject, String> OBJECT_INFO_URL_MAP = new ConcurrentHashMap<>();

    static final Map<String, Integer> EVENT_TYPE_CODE_MAP = new HashMap<>();
    // Tag used to cancel the JSON HTTP request
    static final String tag_json_obj = "json_obj_req/";
    public static World sharedWorld;

    static {
        EVENT_TYPE_CODE_MAP.put(LIST_TYPE_EVENT, LIST_TYPE_EVENT_CODE);
        EVENT_TYPE_CODE_MAP.put(LIST_TYPE_ORGANISATION, LIST_TYPE_ORGANISATION_CODE);
        EVENT_TYPE_CODE_MAP.put(LIST_TYPE_PLACE, LIST_TYPE_PLACE_CODE);
        EVENT_TYPE_CODE_MAP.put(LIST_TYPE_THING, LIST_TYPE_THING_CODE);
        EVENT_TYPE_CODE_MAP.put(LIST_TYPE_NEWS, LIST_TYPE_NEWS_CODE);
    }
    public static final int[] ALL_LIST_TYPE_CODES = {
            LIST_TYPE_EVENT_CODE,
            LIST_TYPE_ORGANISATION_CODE,
            LIST_TYPE_PLACE_CODE,
            LIST_TYPE_THING_CODE,
            LIST_TYPE_NEWS_CODE,
    };

    public static World generateObjects(final Context context) {
        Log.i(CustomWorldHelper5.class.getName(), "generateObjects");

        if (sharedWorld != null) {
            return sharedWorld;
        }
        sharedWorld = new World(context);

        // The user can set the default bitmap. This is useful if you are
        // loading images form Internet and the connection get lost
        sharedWorld.setDefaultImage(R.drawable.beyondar_default_unknown_icon);

        // XXX Workaround bug in Radar Plugin when render without at least one item in every defined ObjectList
        /*
         * java.lang.NullPointerException: Attempt to invoke virtual method 'int com.beyondar.android.world.BeyondarObjectList.size()' on a null object reference
	     * at com.beyondar.android.plugin.radar.RadarView.drawRadarPoints(RadarView.java:69)
         */
        final GeoObject dummyObject = new GeoObject(1L);
        for (int listTypeCode : ALL_LIST_TYPE_CODES) {
            sharedWorld.addBeyondarObject(dummyObject, listTypeCode);
        }


        Log.i(CustomWorldHelper5.class.getName(), "generateObjects() attempting to get History SA data");
        final String historySaUrlParent = "http://data.history.sa.gov.au/sahistoryhub/";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading data...");
        pDialog.show();

        // Object Id needs to be an atomic sequence of Long:
        final AtomicLong objectId = new AtomicLong(2000L);

        for (final String url : HISTORY_SA_LIST_TYPES) {

            final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    historySaUrlParent + url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(final JSONObject response) {
                            // Log.d(CustomWorldHelper5.class.getName(), response.toString());
                            pDialog.hide();

                            Log.i(CustomWorldHelper5.class.getName(), "Got History SA " + url + "s...");
                            try {
                                final JSONArray features = response.getJSONArray("features");
                                if (features != null) {
                                    Log.i(CustomWorldHelper5.class.getName(), "Got History SA " + url + " Features, count=" + features.length());
                                    for (int i = 0; i < features.length(); i++) {
                                        final JSONObject feature = features.getJSONObject(i);
                                        final JSONObject geometry = feature.getJSONObject("geometry");
                                        final JSONArray coordinates = geometry.getJSONArray("coordinates");
                                        final double lng = coordinates.getDouble(0);
                                        final double lat = coordinates.getDouble(1);
                                        final JSONObject properties = feature.getJSONObject("properties");
                                        final String title = properties.getString("TITLE");
                                        final String description = properties.getString("DESCRIPTION");
                                        final String moreInfoUrl = properties.getString("MORE_INFORMATION");

                                        final GeoObject go1 = new GeoObject(objectId.incrementAndGet());
                                        go1.setGeoPosition(lat, lng);
                                        go1.setImageUri("assets://historysalogo-" + url + ".png");
                                        go1.setName(title);
                                        OBJECT_DESCRIPTION_MAP.put(go1, description);
                                        OBJECT_INFO_URL_MAP.put(go1, moreInfoUrl);
                                        // Add the GeoObject to the world
                                        // color-code Places/Events/Organisations/photos etc
                                        sharedWorld.addBeyondarObject(go1, EVENT_TYPE_CODE_MAP.get(url));
                                    }
                                }

                            } catch (JSONException e) {
                                Log.e(CustomWorldHelper5.class.getName(), "Problem fetching GEOJSON \" + url + \" from HistorySA", e);
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(CustomWorldHelper5.class.getName(), "Error: " + error.getMessage());
                    pDialog.hide();
                }
            }) {
            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj + url);
        }


        // Add ABC Online news articles
        final JsonArrayRequest jsonArrReq = new JsonArrayRequest(
                ABC_ONLINE_URL,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(final JSONArray response) {
                        Log.d(CustomWorldHelper5.class.getName(), response.toString());
                        pDialog.hide();

                        if (response == null) {
                            Log.e(CustomWorldHelper5.class.getName(), "ABC Online news response was null");
                            return;
                        }
                        Log.i(CustomWorldHelper5.class.getName(), "Got ABC Online news articles. Count=" + response.length());
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                final JSONObject story = response.getJSONObject(i);
                                final double lng = story.getDouble("Longitude");
                                final double lat = story.getDouble("Latitude");
                                final String title = story.getString("Title");
                                final String description = story.getString("Primary image caption");
                                final String moreInfoUrl = story.getString("URL");
                                final String imageUrl = story.getString("Primary image");
                                final GeoObject go1 = new GeoObject(objectId.incrementAndGet());
                                go1.setGeoPosition(lat, lng);
                                go1.setImageUri(imageUrl);
                                go1.setName(title);
                                OBJECT_DESCRIPTION_MAP.put(go1, description);
                                OBJECT_INFO_URL_MAP.put(go1, moreInfoUrl);

                                // Add the GeoObject to the world
                                // color-code Places/Events/Organisations/photos etc
                                sharedWorld.addBeyondarObject(go1, LIST_TYPE_NEWS_CODE);
                            }

                        } catch (JSONException e) {
                            Log.e(CustomWorldHelper5.class.getName(), "Problem fetching JSON from ABC Online", e);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(CustomWorldHelper5.class.getName(), "Error: " + error.getMessage());
                pDialog.hide();
            }
        }) {
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrReq, tag_json_obj + "news");

        return sharedWorld;
    }

}
