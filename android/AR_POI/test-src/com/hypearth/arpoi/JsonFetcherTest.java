package com.hypearth.arpoi;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bowering on 5/07/2015.
 */
public class JsonFetcherTest extends TestCase {

    public void testReadJsonFromUrl() throws Exception {
        JSONObject historySAPlaces = JsonFetcher.readJsonFromUrl("http://data.history.sa.gov.au/sahistoryhub/place");
        System.out.println("Got History SA Places");
        JSONArray features = historySAPlaces.getJSONArray("features");
        if (features != null) {
            System.out.println("Got History SA Features, count=" + features.length());
            for (int i = 0; i < features.length(); i++) {
                System.out.println("Processing feature " + i);

                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = feature.getJSONArray("coordinates");

                System.out.println("Processing feature " + i + " coords=" + coordinates);

                double lng = coordinates.getDouble(0);
                double lat = coordinates.getDouble(1);
                JSONObject properties = feature.getJSONObject("properties");

                System.out.println("Processing feature " + i + " props=" + properties);

                String title = properties.getString("TITLE");
//                GeoObject go1 = new GeoObject(2000l + i);
//                go1.setGeoPosition(lat,lng);
//                go1.setImageUri("https://data.sa.gov.au/data/uploads/group/20150513-041856.087869historysalogo.png");
//                go1.setName(title);
                // Add the GeoObject to the world

                System.out.println("Processing feature " + i + " completed");
            }
        }
    }
}