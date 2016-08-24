package com.whjpji.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whjpji.sunshine.data.WeatherContract;

public class DetailActivity extends AppCompatActivity {
    private static String LOG_TAG = DetailActivity.class.getSimpleName();
    private String mForecast;
    private DetailFragment mFragment;

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

    public static class DetailFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String FORECAST_ARG = "forecast";
        private ShareActionProvider mShareActionProvider;
        private Uri mForecastUri;
        private String mForecast;
        private TextView mTextView;
        private final int FORECAST_LOADER_ID = 1;

        private final String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        };

        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_MAX_TEMP = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;

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
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detail, menu);

            // Locate MenuItem with ShareActionProvider.
            MenuItem shareItem = menu.findItem(R.id.action_share);

            // Fetch and store ShareActionProvider.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

            // Set share intent.
            setShareIntent(createShareForecastIntent());
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                Bundle args = getArguments();
                mForecast = args.getString(FORECAST_ARG);
            }
            setHasOptionsMenu(true);
        }

        public void setForecast(String forecast) {
            this.mForecast = forecast;
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.fragment_detail, container, false);

            mTextView = (TextView) layout.findViewById(R.id.detailed_forecast_textview);
            mTextView.setText(mForecast);

            return layout;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mForecastUri = intent.getData();
            }
            getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    mForecastUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) return;

            String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));

            String weatherDescription = data.getString(COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric
            );

            String low = Utility.formatTemperature(
                    getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric
            );

            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            TextView textView = (TextView) getView().findViewById(R.id.detailed_forecast_textview);
            textView.setText(mForecast);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }
}
