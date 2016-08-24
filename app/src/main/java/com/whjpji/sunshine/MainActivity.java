package com.whjpji.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private static final String FORECAST_FRAGMENT_TAG = "fragment_tag";
    private String mLocation;
    private boolean mIsMetric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the default settings.
        mLocation = Utility.getPreferredLocation(this);
        mIsMetric = Utility.isMetric(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_main, new ForecastFragment(), FORECAST_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mLocation.equals(Utility.getPreferredLocation(this))) {
            ForecastFragment forecastFragment =
                    (ForecastFragment) getSupportFragmentManager()
                            .findFragmentByTag(FORECAST_FRAGMENT_TAG);
            forecastFragment.onLocationChanged();
            mLocation = Utility.getPreferredLocation(this);
        }
        if (mIsMetric != Utility.isMetric(this)) {
            ForecastFragment forecastFragment =
                    (ForecastFragment) getSupportFragmentManager()
                            .findFragmentByTag(FORECAST_FRAGMENT_TAG);
            forecastFragment.onUnitsChanged();
            mIsMetric = Utility.isMetric(this);
        }
    }
}
