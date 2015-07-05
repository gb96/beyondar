package com.hypearth.arpoi;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Greg Bowering on 5/07/2015.
 */
public abstract class JsonFetcher {

    public static final String TAG = JsonFetcher.class.getSimpleName();

    public static JSONObject readJsonFromUrl(final Context context, final String url) throws JSONException {

        return new JSONObject("");
    }
}
