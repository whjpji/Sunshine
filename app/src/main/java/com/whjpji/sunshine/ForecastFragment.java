package com.whjpji.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.whjpji.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A placeholder fragment containing a list view.
 */
public class ForecastFragment extends Fragment {
    // The adapter of the forecast contents.
    private ForecastAdapter mForecastAdapter;
    // A list view of weather forecast.
    private ListView mForecastListView;
    // Default units of temperature.
    private String mUnits;
    // Shared preferences of the user;
    private SharedPreferences mPreference;

    private static String LOG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Report that this fragment would like to participate in populating the options menu by
         * receiving a call to onCreateOptionsMenu(Menu, MenuInflater) and related methods.
         */
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_forecast, container, false);

        // Here is an example fake data of weather forecasting.
        String [] forecastArray = {
                "Today - Sunny - 88/63",
                "Tommorrow - Foggy - 70/46",
                "Weds - Cloudy - 72/63",
                "Thurs - Rainy - 64/51",
                "Fri - Foggy - 70/46",
                "Sat - Sunny - 76/68"
        };
        List <String> weakForecast = Arrays.asList(forecastArray);

        // Set the location preference.
        // mPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // mLocation = mPreference.getString(
        //         getString(R.string.pref_location_key),
        //         getString(R.string.pref_location_default)
        // );
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis()
        );
        final Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        // Use an array adapter to adapt the forecasting contents to the list view.
        // mForecastAdapter = new ArrayAdapter <>(
        //         getActivity(),
        //         R.layout.list_item_forecast,
        //         R.id.list_item_forcast_textview
        // );
        mForecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);
        mForecastListView = (ListView) layout.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);

        // When an item is clicked, it starts a DetailActivity to display the detailed
        // weather information.

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
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
        new FetchWeatherTask(getActivity()).execute(location);
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

    }

}
