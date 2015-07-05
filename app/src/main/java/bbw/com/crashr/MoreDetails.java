package bbw.com.crashr;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import bbw.com.crashr.db.Incident;
import bbw.com.crashr.db.IncidentHelper;


public class MoreDetails extends AppCompatActivity {

    private Map<Character, String> objectMap_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        setContentView(R.layout.activity_more_details);

        readInIncidents(intent.getStringExtra("INCIDENT"));
    }

    private void readInIncidents(String incidentString) {

        List<Incident> incidents = IncidentHelper.getInstance(this).getIncidents();
        if (incidents == null)
            // Data unavilable
            return;


        // Define the chronological comparator
        SortedSet<Incident> relevant = new TreeSet<>(new Comparator<Incident>() {
            @Override
            public int compare(Incident lhs, Incident rhs) {
                if (lhs.date.before(rhs.date))
                    return 1;
                if (rhs.date.before(lhs.date))
                    return -1;
                return Long.compare(lhs._id, rhs._id);
            }
        });

        // Identify the rel;evant incidents
        incidentLoop:
        for (Incident inc : incidents) {
            boolean containsCause = false;
            for (String cause : inc.causes) {
                String causeStr = IncidentHelper.getInstance(this).getCauseHelper().getCause(cause);
                if (causeStr.equals(incidentString)) {
                    relevant.add(inc);
                    continue incidentLoop;
                }
            }
        }

        // Display the incidents
        long currentTime = System.currentTimeMillis();
        RelativeLayout relLayout = (RelativeLayout) findViewById(R.id.more_detail_layout);
        int incrID = 1;
        TextView prevView = null;
        for (Incident inc : relevant) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            if (incrID == 1)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            else
                params.addRule(RelativeLayout.BELOW, prevView.getId());

            // Add a day string
            String timeAgoStr = createTimeAgoString(currentTime, inc);

            // The view object
            TextView timeAgoView = new TextView(this);
            timeAgoView.setText(timeAgoStr + ":");
            timeAgoView.setTextColor(getResources().getColor(R.color.text_colour));
            timeAgoView.setTypeface(null, Typeface.ITALIC);
            timeAgoView.setLayoutParams(params);
            timeAgoView.setId(incrID++);
            relLayout.addView(timeAgoView);

            // Add detail strings
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (incrID <= 2)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            else
                params.addRule(RelativeLayout.BELOW, prevView.getId());
            params.addRule(RelativeLayout.RIGHT_OF, timeAgoView.getId());

            // Detail string
            String detailString = incidentToString(inc);
            TextView incTextView = new TextView(this);
            incTextView.setText(detailString);
            incTextView.setGravity(1);
            incTextView.setTextColor(getResources().getColor(R.color.text_colour));
            incTextView.setLayoutParams(params);
            incTextView.setId(incrID++);
            relLayout.addView(incTextView);

            prevView = incTextView;
        }
    }

    private String createTimeAgoString(long currentTime, Incident inc) {
        long timeAgo = currentTime - inc.date.getTime();
        String timeAgoStr = "";
        if (timeAgo <= TimeUnit.MINUTES.toMillis(120))
            timeAgoStr = TimeUnit.MINUTES.convert(timeAgo, TimeUnit.MILLISECONDS) + " minutes ago";
        else if (timeAgo <= TimeUnit.HOURS.toMillis(48))
            timeAgoStr = TimeUnit.HOURS.convert(timeAgo, TimeUnit.MILLISECONDS) + " hours ago";
        else if (timeAgo <= TimeUnit.DAYS.toMillis(60))
            timeAgoStr = TimeUnit.DAYS.convert(timeAgo, TimeUnit.MILLISECONDS) + " days ago";
        else if (timeAgo <= TimeUnit.DAYS.toMillis(365))
            timeAgoStr = (int) (TimeUnit.DAYS.convert(timeAgo, TimeUnit.MILLISECONDS) / 7) + " weeks ago";
        else
            timeAgoStr = (int) (TimeUnit.DAYS.convert(timeAgo, TimeUnit.MILLISECONDS) / 365) + " years ago";
        return timeAgoStr;
    }

    /**
     * Converts and incident to a verbose and detailed string.
     *
     * @param inc The incident to convert to string
     * @return A string describing the incident.
     */
    private String incidentToString(Incident inc) {
        StringBuilder incidentString = new StringBuilder();
        // TODO Vehicle type

        // Objects struck
        incidentString.append("Crash involved: " + objectString(inc.objectsStruck));
        // Speed limit
        incidentString.append("\nSpeed limit: " + inc.speedLimit + "km/h");
        // Road wet
        if (inc.roadWet != null) {
            if (inc.roadWet.equalsIgnoreCase("D"))
                incidentString.append("\nDry road");
            if (inc.roadWet.equalsIgnoreCase("I"))
                incidentString.append("\nIcy road");
            if (inc.roadWet.equalsIgnoreCase("W"))
                incidentString.append("\nWet road");
        }

        // Injuries
        boolean newLine = false;
        newLine = true;
        incidentString.append("\n" + inc.minorCount + " minor injuries");
        if (!newLine)
            incidentString.append("\n");
        else
            incidentString.append(", ");
        incidentString.append(inc.severeCount + " severe injuries");
        if (!newLine)
            incidentString.append("\n");
        else
            incidentString.append(", ");
        incidentString.append(inc.fatalCount + " fatalities");

        return incidentString.toString();
    }

    /**
     * Forms string about objects that were struck in the crash.
     *
     * @param objectsStruck The objects struck.
     * @return A comma separated string detailing the objects struck.
     */
    private String objectString(String objectsStruck) {
        ArrayList<String> objects = new ArrayList<>();
        objects.add("another car");
        for (Character c : objectMap_.keySet()) {
            if (objectsStruck.toUpperCase().contains(Character.toUpperCase(c) + ""))
                objects.add(objectMap_.get(c));
        }
        Collections.sort(objects);

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < objects.size(); i++) {
            if (i != 0)
                str.append(", ");
            str.append(objects.get(i));
        }
        String output = str.toString();
        return Character.toUpperCase(output.charAt(0)) + output.substring(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_more_details, menu);
        return true;
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
}
