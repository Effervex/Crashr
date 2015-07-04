package bbw.com.crashr;

import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;


public class CrashrMain extends AppCompatActivity {
    /**
     * Update every 10 seconds.
     */
    private static final long APP_UPDATE_TIME = TimeUnit.SECONDS.toMillis(10);
    private static final int NUM_HAZARDS = 5;
    private Handler locationHandler_;
    private ArrayList<Hazard> hazards_;

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
        // Update the hazard list
        updateHazardList();

        // Update the hazards list
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_layout);
        View prevText = null;
        for (int i = 0; i < hazards_.size(); i++) {
            // Handling location
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i == 0)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            else
                params.addRule(RelativeLayout.BELOW, prevText.getId());

            TextView textView = new TextView(this);
            textView.setText(hazards_.get(i).toString());
            textView.setLayoutParams(params);
            prevText = textView;
            layout.addView(textView);
        }
    }

    /**
     * Method to access database and pull out hazard data.
     * TODO Fill in with information pulled from SQL.
     *
     * @return A collection of hazard data.
     */
    private void updateHazardList() {
        hazards_.clear();
        for (int i = 0; i < NUM_HAZARDS; i++)
            hazards_.add(new Hazard("Hazard #" + i));
    }

    private Runnable updateLocationThread = new Runnable() {

        @Override
        public void run() {
            redrawHazards();
            locationHandler_.postDelayed(this, APP_UPDATE_TIME);
        }
    };
}
