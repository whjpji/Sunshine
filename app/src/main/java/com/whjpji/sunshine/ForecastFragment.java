package com.whjpji.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.TimeZoneFormat;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.whjpji.sunshine.data.WeatherContract;
import com.whjpji.sunshine.service.SunshineService;

/**
 * A placeholder fragment containing a list view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks <Cursor> {
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER_ID = 100;
    private static final String POSITION_ARG = "position";

    // The adapter of the forecast contents.
    private ForecastAdapter mForecastAdapter;

    private ListView mListView;

    private int mPosition = 0;

    private boolean mUseTodayLayout;

    // String array for the Projection.
    private static final String[] FORECAST_COLUMNS = {
            // Since each table has an _id in it, we must use the table name to specify
            // which table the _id belongs to.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTENG = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Report that this fragment would like to participate in populating the options menu by
         * receiving a call to onCreateOptionsMenu(Menu, MenuInflater) and related methods.
         */
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        final String POSITION_ARG = "position";

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_ARG)) {
            mPosition = savedInstanceState.getInt(POSITION_ARG);
            Log.d(LOG_TAG, "onCreateView, read saved position: " + mPosition);
        }

        View layout = inflater.inflate(R.layout.fragment_forecast, container, false);

        mListView = (ListView) layout.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(),
                // or null if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    );
                    // Call the callback function of the main activity.
                    ((Callback) getActivity()).onItemSelected(dateUri);
                    mPosition = position;
                }
            }
        });

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    /**
     * Update the weather information when the locations or metrics change.
     */
    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        // Start the service.
        Intent intent = new Intent(getActivity(), SunshineService.class)
                .putExtra(SunshineService.LOCATION_QUERY_EXTRA, location);
        getActivity().startService(intent);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void onUnitsChanged() {
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
            case R.id.action_viewLocation:
                viewPreferredLocationInMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * View user preferred location in a map via an implicit intent.
     */
    private void viewPreferredLocationInMap() {
        final String GEO_BASE_URI = "geo:0,0?";
        final String QUERY_PARAM = "q";
        String location = Utility.getPreferredLocation(getActivity());
        Uri locationUri = Uri.parse(GEO_BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location).build();
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(locationUri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis()
        );
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public interface Callback {
        void onItemSelected(Uri dateUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Call to save the current selected position.
        if (mPosition != ListView.INVALID_POSITION) {
            Log.d(LOG_TAG, "onCreateView, saved position: " + mPosition);
            outState.putInt(POSITION_ARG, mPosition);
        }
        super.onSaveInstanceState(outState);
    }
}
