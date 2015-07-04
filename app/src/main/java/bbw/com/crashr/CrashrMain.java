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
import android.widget.ViewSwitcher;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import bbw.com.crashr.db.CrashDataSource;
import bbw.com.crashr.db.Incident;

public class CrashrMain extends AppCompatActivity {
    /**
     * Update every 10 seconds.
     */
    private static final long APP_UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);
    private static final int NUM_HAZARDS = 5;
    private static final long ANIM_DURATION = 500;
    private Handler locationHandler_;
    private Hazard[] hazards_;
    private TextSwitcher[] hazardViews_;

    private CauseHelper causeHelper_;

    // SQL access
    public static CrashDataSource dataSource_;
    public static LocationManager locManager_;

    public static Criteria criteria_;
    public static double area_ = 1/110d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hazards_ = new Hazard[NUM_HAZARDS];

        dataSource_ = new CrashDataSource(this);
        dataSource_.open();

        locManager_ = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria_ = new Criteria();
        criteria_.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria_.setCostAllowed(false);

        causeHelper_ = new CauseHelper(getResources());

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
            for (int i = 0; i < hazardViews_.length; i++)
                initSwitcher(hazardViews_[i]);
        }

        // Get SQL data
        Hazard[] newHazards = getHazardRankings(NUM_HAZARDS);
        for (int i = 0; i < NUM_HAZARDS; i++) {
            if (newHazards == null)
                hazardViews_[i].setText("Unavailable");
            else if (hazards_[i] == null || !hazards_[i].equals(newHazards[i]))
                hazardViews_[i].setText(newHazards[i].getText());
        }
        hazards_ = newHazards;
    }

    /**
     * Retrieves the hazard data from the SQL database and processes it to be locally relevant.
     *
     * @param numHazards The number of hazards to return
     */
    private Hazard[] getHazardRankings(int numHazards) {
        Hazard[] hazards = new Hazard[numHazards];

        // Get location
        List<Incident> incidents = getIncidents();
        if (incidents == null)
            return null;

        // Process data
        Map<String, Integer> incidentCounts = processIncidents(incidents);
        Iterator<String> iter = incidentCounts.keySet().iterator();
        for (int i = 0; i < numHazards && iter.hasNext(); i++) {
            // Example data
            String nextKey = iter.next();
            hazards[i] = new Hazard(incidentCounts.get(nextKey) + ": " + nextKey, nextKey);
        }
        return hazards;
    }

    private List<Incident> getIncidents() {
        String provider = locManager_.getBestProvider(criteria_, false);
        Location location = locManager_.getLastKnownLocation(provider);
        if (location == null)
            return null;
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        return dataSource_.getLocalisedIncidents(lat - area_, lat + area_, lon - area_, lon + area_);
    }

    /**
     * Processes the incidents, gathering the data together into counts.
     *
     * @param incidents The incidents to process.
     * @return A sorted map of incidents, from most to least
     */
    private SortedMap<String,Integer> processIncidents(List<Incident> incidents) {
        Map<String, Integer> countMap = new HashMap<>();
        for (Incident inc : incidents) {
            String[] causes = inc.causes;
            for (String cause : causes) {
                if (cause.isEmpty())
                    continue;
                String causeCategory = causeHelper_.getCause(cause);
                if (causeCategory.equals("ERROR"))
                    continue;
                if (!countMap.containsKey(causeCategory))
                    countMap.put(causeCategory, 1);
                else
                    countMap.put(causeCategory, countMap.get(causeCategory) + 1);
            }
        }

        Comparator<String> comparison = new ValueComparator<>(countMap);
        SortedMap<String, Integer> sortedMap = new TreeMap<>(comparison);
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
        List<Incident> incidents = getIncidents();
//        if (incidents == null)
            // Data unavilable

        System.out.println("Triggered by " + view.toString());
    }
}
