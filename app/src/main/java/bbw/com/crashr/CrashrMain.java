package bbw.com.crashr;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import bbw.com.crashr.db.CrashDataSource;
import bbw.com.crashr.db.Incident;
import bbw.com.crashr.db.IncidentHelper;
import bbw.com.crashr.ml.NaiveBayes;

public class CrashrMain extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    /**
     * Update every 10 seconds.
     */
    private static final long APP_UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);
    private static final int NUM_HAZARDS = 5;
    private static final long ANIM_DURATION = 500;
    private Handler locationHandler_;
    private Hazard[] hazards_;
    private TextSwitcher[] hazardViews_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hazards_ = new Hazard[NUM_HAZARDS];

        IncidentHelper.getInstance(this);

        locationHandler_ = new Handler();
        locationHandler_.postDelayed(updateLocationThread, 0);

        setContentView(R.layout.activity_crashr_main);
    }

    private void initSwitcher(TextSwitcher switcher) {
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView view = new TextView(CrashrMain.this);
                view.setTextSize(24);
                view.setTextColor(getResources().getColor(R.color.text_colour));
                return view;
            }
        });

        Animation in = AnimationUtils.loadAnimation(this, R.anim.text_slide_in);
        in.setDuration(ANIM_DURATION);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.text_slide_out);
        out.setDuration(ANIM_DURATION);


        switcher.setInAnimation(in);
        switcher.setOutAnimation(out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crashr_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_map);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startMap();
                return true;
            }
        });

        return true;
    }

    public void startMap() {
        startActivity(new Intent(this, HeatmapsDemoActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void redrawHazards() {
        if (hazardViews_ == null) {
            hazardViews_ = new TextSwitcher[NUM_HAZARDS];
            hazardViews_[0] = (TextSwitcher) findViewById(R.id.hazard1);
            hazardViews_[1] = (TextSwitcher) findViewById(R.id.hazard2);
            hazardViews_[2] = (TextSwitcher) findViewById(R.id.hazard3);
            hazardViews_[3] = (TextSwitcher) findViewById(R.id.hazard4);
            hazardViews_[4] = (TextSwitcher) findViewById(R.id.hazard5);
            for (TextSwitcher ts : hazardViews_)
                initSwitcher(ts);
        }

        // Get SQL data
        Hazard[] newHazards = getHazardRankings(NUM_HAZARDS);
        for (int i = 0; i < NUM_HAZARDS; i++) {
            if (newHazards == null)
                hazardViews_[i].setText("Unavailable");
            else if (newHazards[i] == null || i >= newHazards.length)
                hazardViews_[i].setText("");
            else if (hazards_[i] == null || !hazards_[i].equals(newHazards[i])) {
                hazardViews_[i].setText(newHazards[i].getText());
            }
        }
        if (newHazards == null)
            hazards_ = new Hazard[NUM_HAZARDS];
        else
            hazards_ = Arrays.copyOf(newHazards, NUM_HAZARDS);
    }

    /**
     * Retrieves the hazard data from the SQL database and processes it to be locally relevant.
     *
     * @param numHazards The number of hazards to return
     */
    private Hazard[] getHazardRankings(int numHazards) {
        Hazard[] hazards = new Hazard[numHazards];

        // Get location
        List<Incident> incidents = IncidentHelper.getInstance(this).getIncidents();
        if (incidents == null)
            return null;

        // Process data
        Map<String, Double> incidentCounts = processIncidents(incidents);
        Iterator<String> iter = incidentCounts.keySet().iterator();
        for (int i = 0; i < numHazards && iter.hasNext(); i++) {
            // Example data
            String nextKey = IncidentHelper.getInstance(this).getCauseHelper().getCause(iter.next());
            hazards[i] = new Hazard(nextKey);
        }
        return hazards;
    }

    /**
     * Processes the incidents, gathering the data together into counts.
     *
     * @param incidents The incidents to process.
     * @return A sorted map of incidents, from most to least
     */
    private SortedMap<String,Double> processIncidents(List<Incident> incidents) {

        Location loc = IncidentHelper.getInstance(this).getLocation();

        Incident inc = new Incident();
        inc.date = new Date();
        inc.weather = Weather.getWeatherCode(loc.getLatitude(), loc.getLongitude());

        NaiveBayes nb = new NaiveBayes(10);
        nb.train(incidents);
        Map<String, Double> countMap = nb.predict(inc);

        Comparator<String> comparison = new ValueComparator<>(countMap);
        SortedMap<String, Double> sortedMap = new TreeMap<>(comparison);
        sortedMap.putAll(countMap);
        return sortedMap;
    }

    /**
     * A ongoing update thread for querying the location.
     */
    private Runnable updateLocationThread = new Runnable() {

        @Override
        public void run() {
            redrawHazards();
            locationHandler_.postDelayed(this, APP_UPDATE_TIME);
        }
    };

    public void moreHazardInfo(View view) {
        // Open a new Intent
        TextView textView = (TextView) ((TextSwitcher) view).getCurrentView();
        String viewString = textView.getText().toString();
        Intent intent = new Intent(this, MoreDetails.class);
        intent.putExtra("INCIDENT", viewString);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        IncidentHelper.getInstance(this).onConnected(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "location changed", Toast.LENGTH_SHORT);
    }
}
