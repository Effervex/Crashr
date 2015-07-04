package bbw.com.crashr;

import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class CrashrMain extends AppCompatActivity {
    /**
     * Update every 10 seconds.
     */
    private static final long APP_UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);
    private static final int NUM_HAZARDS = 5;
    private Handler locationHandler_;
    private ArrayList<Hazard> hazards_;
    private ArrayList<TextView> hazardViews_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hazards_ = new ArrayList<>();

        locationHandler_ = new Handler();
        locationHandler_.postDelayed(updateLocationThread, 0);

        setContentView(R.layout.activity_crashr_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crashr_main, menu);
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

    private void redrawHazards() {
        // Get SQL data
        ArrayList<Hazard> newHazards = getHazardRankings(NUM_HAZARDS);

        // Update the hazards list
        View prevText = null;
        ArrayList<TextView> newViews = new ArrayList<>();
        for (int i = 0; i < newHazards.size(); i++) {
            Hazard currentHazard = newHazards.get(i);
            // Check if the old hazards contained a hazard of the same type
            TextView currentView = null;
            if (hazards_.contains(newHazards.get(i))) {
                int index = hazards_.indexOf(newHazards.get(i));
                Hazard existing = hazards_.get(index);
                existing.setText(currentHazard.getText());
                newHazards.set(i, existing);
                // TODO Animate movement of hazards

                currentView = moveView(index, i, existing);
            } else {
                currentView = addView(i, currentHazard, newViews);
            }
            newViews.add(currentView);
        }
        for (Hazard hazard : hazards_) {
            if (!newHazards.contains(hazard))
                removeView(hazards_.indexOf(hazard), hazard);
        }
        hazards_ = newHazards;
        hazardViews_ = newViews;
    }

    private void removeView(int startLocation, Hazard removedHazard) {

    }

    private TextView addView(int endLocation, Hazard addedHazard, ArrayList<TextView> newViews) {
        // Handling location
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_layout);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        if (endLocation == 0)
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        else
            params.addRule(RelativeLayout.BELOW, newViews.get(endLocation - 1).getId());
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        TextView newView = new TextView(this);
        newView.setText(addedHazard.getText());
        newView.setTextSize(32);
        newView.setLayoutParams(params);
        newView.setId(endLocation + 1);
        newView.setTextColor(getResources().getColor(R.color.text_colour));

        layout.addView(newView);
        float location = newView.getY();
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -50, location);

        newView.setAnimation(translateAnimation);

        return newView;
    }

    private TextView moveView(int startLocation, int endLocation, Hazard movedHazard) {
        return null;
    }

    /**
     * Retrieves the hazard data from the SQL database and processes it to be locally relevant.
     */
    private ArrayList<Hazard> getHazardRankings(int numHazards) {
        ArrayList<Hazard> hazards = new ArrayList<>();
        for (int i = 0; i< numHazards; i++) {
            // Example data
            hazards.add(new Hazard("Example Hazard #" + i, "Alcohol" + i));
        }
        return hazards;
    }

    private Runnable updateLocationThread = new Runnable() {

        @Override
        public void run() {
            redrawHazards();
            locationHandler_.postDelayed(this, APP_UPDATE_TIME);
        }
    };
}
