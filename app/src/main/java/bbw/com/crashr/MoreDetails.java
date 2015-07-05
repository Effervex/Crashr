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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import bbw.com.crashr.db.Incident;
import bbw.com.crashr.db.IncidentHelper;


public class MoreDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
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
            long timeAgo = currentTime - inc.date.getTime();
            String timeAgoStr = "";
            if (timeAgo <= TimeUnit.MINUTES.toMillis(120))
                timeAgoStr = TimeUnit.MINUTES.convert(timeAgo, TimeUnit.MILLISECONDS) + " minutes ago";
            else if (timeAgo <= TimeUnit.HOURS.toMillis(48))
                timeAgoStr = TimeUnit.HOURS.convert(timeAgo, TimeUnit.MILLISECONDS) + " hours ago";
            else if (timeAgo <= TimeUnit.DAYS.toMillis(365))
                timeAgoStr = TimeUnit.DAYS.convert(timeAgo, TimeUnit.MILLISECONDS) + " days ago";
            else
                timeAgoStr = (int) (TimeUnit.DAYS.convert(timeAgo, TimeUnit.MILLISECONDS) / 365) + " years ago";

            // The view object
            TextView timeAgoView = new TextView(this);
            timeAgoView.setText(timeAgoStr + ":");
            timeAgoView.setTextColor(getResources().getColor(R.color.text_colour));
            timeAgoView.setTypeface(null, Typeface.ITALIC);
            timeAgoView.setLayoutParams(params);
            relLayout.addView(timeAgoView);

            // Add detail strings
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            if (incrID == 1)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            else
                params.addRule(RelativeLayout.BELOW, prevView.getId());

            // Detail string
            String detailString = incidentToString(inc);
            TextView incTextView = new TextView(this);
            incTextView.setText(detailString);
            incTextView.setTextColor(getResources().getColor(R.color.text_colour));
            incTextView.setLayoutParams(params);
            incTextView.setId(incrID++);
            relLayout.addView(incTextView);

            prevView = incTextView;
        }
    }

    /**
     * Converts and incident to a verbose and detailed string.
     *
     * @param inc
     * @return
     */
    private String incidentToString(Incident inc) {
        return null;
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
