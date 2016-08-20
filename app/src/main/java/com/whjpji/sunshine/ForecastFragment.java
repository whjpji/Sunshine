package com.whjpji.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private ArrayAdapter <String> mForecastAdapter;
    // A list view of weather forecast.
    private ListView mForecastListView;
    // The postal code of the city to query.
    private String mLocation;
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
        mPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLocation = mPreference.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );

        // Use an array adapter to adapt the forecasting contents to the list view.
        mForecastAdapter = new ArrayAdapter <>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forcast_textview
        );
        mForecastListView = (ListView) layout.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);

        // When an item is clicked, it starts a DetailActivity to display the detailed
        // weather information.
        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Start a new activity to display the detailed weather forecast.
                String forecast = mForecastAdapter.getItem((int) id);
                // makeToast(forecast);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    /**
     * Make a toast to display some text on the screen.
     * @param text the text string to display.
     */
    private void makeToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
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
        mUnits = mPreference.getString(
                getString(R.string.pref_units_key),
                getString(R.string.pref_units_default)
        );
        updateLocation();
        new FetchWeatherTask().execute(mLocation, mUnits);
    }

    /**
     * Update the user preferred location from the shared preference.
     */
    private void updateLocation() {
        mLocation = mPreference.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );
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
        updateLocation();

        Uri geoLocation = Uri.parse(GEO_BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, mLocation)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Format the highest and lowest temperature string of the day, rounding it into integer.
     * @param high the highest temperature of the day.
     * @param low the lowest temperature of the day.
     * @return the formatted string "high/low" of the temperature.
     */
    private String formatHighLowTemperature(double high, double low) {
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        return roundedHigh + "/" + roundedLow;
    }

    /**
     * Format the date information from the time given in milliseconds.
     * @param time date information given in milliseconds.
     * @return the formatted date information "EEE MM dd".
     */
    private String getReadableDateString(long time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MM dd");
        return shortenedDateFormat.format(time);
    }

    /** Get and format weather data from the json string.
     * @param forecastJsonStr json string of the forecast information.
     * @throws JSONException exceptions may be caught if there are some error in the json string.
     * @return parsed forecast weather data strings.
     */
    @NonNull
    private String [] getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {

        // There are some names of the JSON object that need to be extracted.
        final String OWN_LIST = "list";
        final String OWN_WEATHER = "weather";
        final String OWN_TEMPERATURE = "temp";
        final String OWN_MAX = "max";
        final String OWN_MIN = "min";
        final String OWN_DAYS = "cnt";
        final String OWN_DESCRIPTION = "description";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWN_LIST);
        int numDays = forecastJson.getInt(OWN_DAYS);

        Time time = new Time();
        time.setToNow();

        // Get local time.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);

        time = new Time();

        String [] resultStrs = new String [numDays];
        for (int i = 0; i < numDays; ++i) {
            // Result format: "date - description - high/low".
            String date;
            String description;
            String highAndLow;

            // Get the JSON object representing the day.
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // Get human-readable date string converted from dateTime returned by setJulianDay.
            long dateTime = time.setJulianDay(julianStartDay + i);
            date = getReadableDateString(dateTime);

            // Get weather description for the day.
            JSONObject weatherObject = dayForecast.getJSONArray(OWN_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWN_DESCRIPTION);

            // Get the highest and lowest temperature of the day.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWN_TEMPERATURE);
            double high = temperatureObject.getDouble(OWN_MAX);
            double low = temperatureObject.getDouble(OWN_MIN);
            highAndLow = formatHighLowTemperature(high, low);

            // Assemble it to result string.
            String weatherStr = date + " - " + description + " - " + highAndLow;
            resultStrs[i] = weatherStr;
        }
        return resultStrs;
    }

    private class FetchWeatherTask extends AsyncTask <String, Void, String []> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private final String FORECAST_BASE_URL =
                "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private final String QUERY_PARAM = "q";
        private final String FORMAT_PARAM = "mode";
        private final String UNITS_PARAM = "units";
        private final String DAYS_PARAM = "cnt";
        private final String APPID_PARAM = "appid";

        @Override
        protected String [] doInBackground(String... params) {
            // Url parameters
            String location = params[0];
            String format = "json";
            String units = params[1];
            int numDays = 7;


            // Use Uri.Builder to build an url with parameters
            String url = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, location)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build().toString();

            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            Response response = null;
            try {
                response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    return getWeatherDataFromJson(response.body().string());
                } else {
                    Log.e(LOG_TAG, "Http request failed.");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Log.e(LOG_TAG, e.toString());
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (response != null)
                    response.close();
            }
        }

        @Override
        protected void onPostExecute(String [] result) {
            if (result != null) {
                // Update the data of the adapter.
                mForecastAdapter.clear();
                for (String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }
                mForecastAdapter.notifyDataSetChanged();
            }
        }
    }
}
