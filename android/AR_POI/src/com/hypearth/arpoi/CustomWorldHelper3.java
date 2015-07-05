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

public class CustomWorldHelper3 {
    public static final int LIST_TYPE_EVENT = "event".hashCode();
    public static final int LIST_TYPE_ORGANISATION = "organisation".hashCode();
    public static final int LIST_TYPE_PLACE = "place".hashCode();
    public static final Map<GeoObject, String> OBJECT_DESCRIPTION_MAP = new HashMap<>();
    // Tag used to cancel the JSON HTTP request
    static final String tag_json_obj = "json_obj_req";
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

        final String[] urls = {urlEvent, urlOrganisation, urlPlace};

        for (final String url : urls) {
            final ProgressDialog pDialog = new ProgressDialog(context);
            pDialog.setMessage("Loading History SA " + url + "s...");
            pDialog.show();

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    urlParent + url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(final JSONObject response) {
                            Log.d(CustomWorldHelper3.class.getName(), response.toString());
                            pDialog.hide();

                            Log.i(CustomWorldHelper3.class.getName(), "Got History SA " + url + "s...");
                            try {
                                JSONArray features = response.getJSONArray("features");
                                if (features != null) {
                                    Log.i(CustomWorldHelper3.class.getName(), "Got History SA " + url + " Features, count=" + features.length());
                                    for (int i = 0; i < features.length(); i++) {
                                        JSONObject feature = features.getJSONObject(i);
                                        JSONObject geometry = feature.getJSONObject("geometry");
                                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                                        double lng = coordinates.getDouble(0);
                                        double lat = coordinates.getDouble(1);
                                        JSONObject properties = feature.getJSONObject("properties");
                                        String title = properties.getString("TITLE");
                                        String description = properties.getString("DESCRIPTION");

                                        GeoObject go1 = new GeoObject(2000l + i);
                                        go1.setGeoPosition(lat, lng);
                                        go1.setImageUri("assets://historysalogo-" + url + ".png");
                                        go1.setName(title);
                                        OBJECT_DESCRIPTION_MAP.put(go1, description);
                                        // Add the GeoObject to the world
                                        // TODO color-code Places/Events/Organisations/photos etc
                                        sharedWorld.addBeyondarObject(go1, url.hashCode());
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
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
            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        }


        return sharedWorld;
    }

}
