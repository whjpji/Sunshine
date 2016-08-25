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
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.whjpji.sunshine.data.WeatherContract;

/**
 * Created by whjpji on 16-8-24.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_URI_ARG = "forecast uri";
    private final static String FORECAST_SHARE_HASHING = "#SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private Uri mDateUri;
    private String mForecast;

    // Layout views on the fragment.
    private ImageView mIconView;
    private TextView mDateView;
    private TextView mDayView;
    private TextView mHighView;
    private TextView mLowView;
    private TextView mDescriptionView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    private final int FORECAST_LOADER_ID = 1;

    private final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_CONDITION_ID = 3;
    private static final int COL_WEATHER_MAX_TEMP = 4;
    private static final int COL_WEATHER_MIN_TEMP = 5;
    private static final int COL_WEATHER_HUMIDITY = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_PRESSURE = 9;

    public DetailFragment() {
    }

    public static DetailFragment newInstance(Uri dateUri) {

        Bundle args = new Bundle();

        DetailFragment fragment = new DetailFragment();
        args.putParcelable(FORECAST_URI_ARG, dateUri);
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
            if (args != null) {
                mDateUri = args.getParcelable(FORECAST_URI_ARG);
            }
        }
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    /**
     * Call to update the share intent.
     *
     * @param shareIntent the share intent to update.
     */
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Create an intent for sharing the detailed forecast data.
     *
     * @return
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHING);
        Log.d(LOG_TAG, "Created share intent with \"" + shareIntent.getStringExtra(Intent.EXTRA_TEXT));
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) layout.findViewById(R.id.icon_imageview);
        mDateView = (TextView) layout.findViewById(R.id.date_textview);
        mDayView = (TextView) layout.findViewById(R.id.day_textview);
        mDescriptionView = (TextView) layout.findViewById(R.id.weather_description_textview);
        mHighView = (TextView) layout.findViewById(R.id.high_textview);
        mLowView = (TextView) layout.findViewById(R.id.low_textview);
        mHumidityView = (TextView) layout.findViewById(R.id.humidity_textview);
        mWindView = (TextView) layout.findViewById(R.id.wind_textview);
        mPressureView = (TextView) layout.findViewById(R.id.pressure_textview);


        return layout;
    }

    public void onUnitsChanged() {
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void onLocationChanged(String newLocation) {
        if (mDateUri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(mDateUri);
            mDateUri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create a new CursorLoader.
        return new CursorLoader(getActivity(),
                mDateUri,
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (data == null || !data.moveToFirst()) return;

        // Set the image icon view.
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        // Set the date text view.
        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        mDateView.setText(dateString);

        // Set the day text view.
        String dayString = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
        mDayView.setText(dayString);

        // Set the weather description text view.
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());

        // Set the highest temperature text view.
        String high = Utility.formatTemperature(
                getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric
        );
        mHighView.setText(high);

        // Set the lowest temperature text view.
        String low = Utility.formatTemperature(
                getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric
        );
        mLowView.setText(low);

        // Set the humidity text view.
        String humidity = Utility.getFormattedHumidity(
                getActivity(), data.getFloat(COL_WEATHER_HUMIDITY)
        );
        mHumidityView.setText(humidity);

        // Set the wind text view.
        float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float degrees = data.getFloat(COL_WEATHER_DEGREES);
        String wind = Utility.getFormattedWind(getActivity(), windSpeed, degrees);
        mWindView.setText(wind);

        // Set the pressure text view.
        String pressure = Utility.getFormattedPressure(
                getActivity(), data.getFloat(COL_WEATHER_PRESSURE)
        );
        mPressureView.setText(pressure);

        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
