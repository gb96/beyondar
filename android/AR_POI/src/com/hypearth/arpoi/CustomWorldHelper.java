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

public class CustomWorldHelper {
    public static final int LIST_TYPE_EXAMPLE_1 = 1;

    // Tag used to cancel the JSON HTTP request
    static final String tag_json_obj = "json_obj_req";

    public static World sharedWorld;

    public static World generateObjects(Context context) {
        Log.i(CustomWorldHelper.class.getName(), "generateObjects");

        if (sharedWorld != null) {
            return sharedWorld;
        }
        sharedWorld = new World(context);

        // The user can set the default bitmap. This is useful if you are
        // loading images form Internet and the connection get lost
        sharedWorld.setDefaultImage(R.drawable.beyondar_default_unknown_icon);

        // sharedWorld.setGeoPosition(41.90533734214473d, 2.565848038959814d);

            Log.i(CustomWorldHelper.class.getName(), "generateObjects() attempting to get History SA places");
        String url = "http://data.history.sa.gov.au/sahistoryhub/place";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading History SA places...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(CustomWorldHelper.class.getName(), response.toString());
                        pDialog.hide();

                        Log.i(CustomWorldHelper.class.getName(), "Got History SA Places");
                        try {
                            JSONArray features = response.getJSONArray("features");
                            if (features != null) {
                                Log.i(CustomWorldHelper.class.getName(), "Got History SA Features, count=" + features.length());
                                for (int i = 0; i < features.length(); i++) {
                                    JSONObject feature = features.getJSONObject(i);
                                    JSONObject geometry = feature.getJSONObject("geometry");
                                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                                    double lng = coordinates.getDouble(0);
                                    double lat = coordinates.getDouble(1);
                                    JSONObject properties = feature.getJSONObject("properties");
                                    String title = properties.getString("TITLE");
                                    GeoObject go1 = new GeoObject(2000l + i);
                                    go1.setGeoPosition(lat, lng);
                                    go1.setImageUri("https://data.sa.gov.au/data/uploads/group/20150513-041856.087869historysalogo.png");
                                    go1.setName(title);
                                    // Add the GeoObject to the world
                                    sharedWorld.addBeyondarObject(go1);
                                    // sharedWorld.addBeyondarObject(go1, LIST_TYPE_EXAMPLE_1); // TODO color-code Places/Events/Organisations/photos etc
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(CustomWorldHelper.class.getName(), "Error: " + error.getMessage());
                pDialog.hide();
                }
        }) {

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

        return sharedWorld;
    }

}
