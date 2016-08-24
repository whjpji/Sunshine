package com.whjpji.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {
    private static String LOG_TAG = DetailActivity.class.getSimpleName();
    private String mForecast;
    private DetailFragment mFragment;
    private ShareActionProvider mShareActionProvider;

    private final static String FORECAST_SHARE_HASHING = "#SunshineApp";

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        // Locate MenuItem with ShareActionProvider.
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        // Set share intent.
        setShareIntent(createShareForecastIntent());

        return true;
    }

    /**
     * Call to update the share intent.
     * @param shareIntent the share intent to update.
     */
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Create an intent for sharing the detailed forecast data.
     * @return
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHING);
        return shareIntent;
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

    public static class DetailFragment extends Fragment {
        private static final String FORECAST_ARG = "forecast";
        private String mForecast;
        private TextView mTextView;

        public DetailFragment() {
        }

        public static DetailFragment newInstance(String forecast) {
            
            Bundle args = new Bundle();
            
            DetailFragment fragment = new DetailFragment();
            args.putString(FORECAST_ARG, forecast);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                Bundle args = getArguments();
                mForecast = args.getString(FORECAST_ARG);
            }
        }

        public void setForecast(String forecast) {
            this.mForecast = forecast;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.fragment_detail, container, false);

            mTextView = (TextView) layout.findViewById(R.id.detailed_forecast_textview);
            mTextView.setText(mForecast);

            return layout;
        }
    }
}
