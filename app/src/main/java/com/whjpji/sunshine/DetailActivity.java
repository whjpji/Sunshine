package com.whjpji.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {
    private static String LOG_TAG = DetailActivity.class.getSimpleName();
    private String mForecast;
    private DetailFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get data from the intent.
        Intent intent = getIntent();
        if (intent != null) {
            mForecast = intent.getDataString();
        }
        Log.i(LOG_TAG, mForecast);

        if (savedInstanceState == null) {
            mFragment = DetailFragment.newInstance(mForecast);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        } else {
            mFragment.setForecast(mForecast);
        }
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

}
