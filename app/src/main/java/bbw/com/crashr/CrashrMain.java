package bbw.com.crashr;

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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class CrashrMain extends AppCompatActivity {
    /**
     * Update every 10 seconds.
     */
    private static final long APP_UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);
    private static final int NUM_HAZARDS = 5;
    private Handler locationHandler_;
    private Hazard[] hazards_;
    private TextSwitcher[] hazardViews2_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hazards_ = new Hazard[NUM_HAZARDS];

        locationHandler_ = new Handler();
        locationHandler_.postDelayed(updateLocationThread, 0);

        setContentView(R.layout.activity_crashr_main);
    }

    private void initSwitcher(TextSwitcher switcher) {
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView view = new TextView(CrashrMain.this);
                view.setTextSize(32);
                view.setTextColor(getResources().getColor(R.color.text_colour));
                return view;
            }
        });

        Animation in = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        switcher.setInAnimation(in);
        switcher.setOutAnimation(out);
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
        if (hazardViews2_ == null) {
            hazardViews2_ = new TextSwitcher[NUM_HAZARDS];
            hazardViews2_[0] = (TextSwitcher) findViewById(R.id.hazard1);
            hazardViews2_[1] = (TextSwitcher) findViewById(R.id.hazard2);
            hazardViews2_[2] = (TextSwitcher) findViewById(R.id.hazard3);
            hazardViews2_[3] = (TextSwitcher) findViewById(R.id.hazard4);
            hazardViews2_[4] = (TextSwitcher) findViewById(R.id.hazard5);
            for (int i = 0; i < hazardViews2_.length; i++)
                initSwitcher(hazardViews2_[i]);
        }

        // Get SQL data
        Hazard[] newHazards = getHazardRankings(NUM_HAZARDS);

        for (int i = 0; i < newHazards.length; i++) {
            if (hazards_[i] == null || !hazards_[i].equals(newHazards[i]))
                hazardViews2_[i].setText(newHazards[i].getText());
        }
        hazards_ = newHazards;
    }

    /**
     * Retrieves the hazard data from the SQL database and processes it to be locally relevant.
     */
    private Hazard[] getHazardRankings(int numHazards) {
        Hazard[] hazards = new Hazard[numHazards];
        for (int i = 0; i < numHazards; i++) {
            // Example data
            hazards[i] = new Hazard("Example Hazard #" + i, "Alcohol" + i);
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
