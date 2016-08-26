package com.whjpji.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT_TAG = "fragment_tag";
    private String mForecast;
    private DetailFragment mFragment;
    private boolean mIsMetric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Get data from the intent.
            Intent intent = getIntent();
            Uri data = null;
            if (intent != null) {
                data = intent.getData();
            }
            if (data != null) {
                // Build fragment with given uri data.
                DetailFragment detailFragment = DetailFragment.newInstance(data);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

    protected void onResume() {
        super.onResume();
        if (mIsMetric != Utility.isMetric(this)) {
            DetailFragment detailFragment =
                    (DetailFragment) getSupportFragmentManager()
                            .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onUnitsChanged();
            }
            mIsMetric = Utility.isMetric(this);
        }
    }
}
