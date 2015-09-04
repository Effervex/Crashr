/*
 * Copyright 2014 Google Inc.
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

package bbw.com.crashr;

import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import com.google.maps.android.demo.BaseDemoActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import bbw.com.crashr.db.CrashDataSource;
import bbw.com.crashr.db.Incident;
import bbw.com.crashr.db.IncidentHelper;

/**
 * A demo of the Heatmaps library. Demonstrates how the HeatmapTileProvider can be used to create
 * a colored map overlay that visualises many points of weighted importance/intensity, with
 * different colors representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsDemoActivity extends BaseDemoActivity {

    private final double HAMILTON_LNG = 175.301396;
    private final double HAMILTON_LAT = 37.786127;

    private static final long APP_UPDATE_TIME = TimeUnit.SECONDS.toMillis(30);

    /**
     * A ongoing update thread for querying the location.
     */
    private Runnable updateLocationThread = new Runnable() {

        @Override
        public void run() {
            updateMap();
            locationHandler_.postDelayed(this, APP_UPDATE_TIME);
        }
    };

    private Handler locationHandler_;


    private static final int RADIUS = 30;
    private static final float OPACITY = 0.75f;


    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private HashMap<String, DataSet> mLists = new HashMap<String, DataSet>();

    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    private void updateMap() {

        List<Incident> incidents = IncidentHelper.getInstance(this).getIncidents();
        if (incidents == null)
            return;

        try {
            //mLists.put("crashes", new DataSet(readItems(R.raw.tmp)));
            mLists.put("crashes", new DataSet(readIncidents(incidents)));
        } catch (Exception e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        if( mLists.get("crashes").getData().size() != 0 ) {
            if (mProvider == null) {
                mProvider = new HeatmapTileProvider.Builder().data(
                        mLists.get("crashes").getData()).build();
                mProvider.setRadius(RADIUS);
                mProvider.setOpacity(OPACITY);
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            } else {
                mProvider.setData(mLists.get("crashes").getData());
                mOverlay.clearTileCache();
            }
        }

        // ok now move the map to the user's current location
        Location loc = IncidentHelper.getInstance(this).getLocation();
        if(loc != null) {
            LatLng coordinate = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coordinate,
                    getMap().getCameraPosition().zoom);
            getMap().moveCamera( update );
        }
        // do the right zoom

    }

    @Override
    protected void startDemo() {

        locationHandler_ = new Handler();
        locationHandler_.postDelayed(updateLocationThread, 0);

        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(HAMILTON_LAT, HAMILTON_LNG), 4));

        getMap().setMyLocationEnabled(true);

        //updateMap();
    }

    private ArrayList<LatLng> readIncidents( List<Incident> incidents ) {
        ArrayList<LatLng> list = new ArrayList<>();
        for(Incident incident : incidents) {
            list.add( new LatLng(incident.latitude, incident.longitude) );
        }
        return list;
    }

    // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    /**
     * Helper class - stores data sets and sources.
     */
    private class DataSet {
        private ArrayList<LatLng> mDataset;

        public DataSet(ArrayList<LatLng> dataSet) {
            this.mDataset = dataSet;
        }

        public ArrayList<LatLng> getData() {
            return mDataset;
        }
    }

}
