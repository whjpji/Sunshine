package com.whjpji.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.whjpji.sunshine.data.WeatherContract;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {
    private static final String FORECAST_FRAGMENT_TAG = "fragment_tag";
    public static final String DETAIL_FRAGMENT_TAG = "detail_tag";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mLocation;
    private boolean mIsMetric;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the default settings.
        mLocation = Utility.getPreferredLocation(this);
        mIsMetric = Utility.isMetric(this);

        if (findViewById(R.id.container_detail) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                // Get the uri of today's weather.
                String locationSetting = Utility.getPreferredLocation(this);
                Uri data = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting, System.currentTimeMillis()
                );

                // Create the detail fragment and set the data.
                DetailFragment detailFragment = DetailFragment.newInstance(data);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_detail, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        Log.d(LOG_TAG, "onCreate, twoPane: " + mTwoPane);
        ForecastFragment forecastFragment = (ForecastFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);
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
            mLocation = Utility.getPreferredLocation(this);
            // Call the onLocationChangedMethod of ForecastFragment.
            ForecastFragment forecastFragment =
                    (ForecastFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_forecast);
            if (forecastFragment != null) {
                forecastFragment.onLocationChanged();
            }

            // Call the onLocationChangedMethod of DetailFragment.
            DetailFragment detailFragment =
                    (DetailFragment) getSupportFragmentManager()
                            .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onLocationChanged(mLocation);
            }
        }
        if (mIsMetric != Utility.isMetric(this)) {
            mIsMetric = Utility.isMetric(this);
            // Call the onUnitsChanged method of ForecastFragment.
            ForecastFragment forecastFragment =
                    (ForecastFragment) getSupportFragmentManager()
                            .findFragmentByTag(FORECAST_FRAGMENT_TAG);
            if (forecastFragment != null) {
                forecastFragment.onUnitsChanged();
            }

            // Call the onUnitsChanged method of DetailFragment.
            DetailFragment detailFragment =
                    (DetailFragment) getSupportFragmentManager()
                            .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onUnitsChanged();
            }
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            // For two panes, just replace the detail fragment with a new one.
            DetailFragment detailFragment = DetailFragment.newInstance(dateUri);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_detail, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            // Else, start a new activity to hold the fragment.
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
