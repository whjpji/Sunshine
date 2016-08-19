package com.whjpji.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
    // the postal code of the city to query.
    private String mPostalCode = "94043";

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

        // Use an array adapter to adapt the forecasting contents to the list view.
        // mForecastAdapter = new ArrayAdapter <>(
        //         getActivity(),
        //         R.layout.list_item_forecast,
        //         R.id.list_item_forcast_textview,
        //         weakForecast
        // );
        mForecastListView = (ListView) layout.findViewById(R.id.listview_forecast);
        // mForecastListView.setAdapter(mForecastAdapter);
        new FetchWeatherTask().execute(mPostalCode);

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new FetchWeatherTask().execute(mPostalCode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Parses the json string response
     * @param forecastJsonStr json string of the forecast information.
     * @return parsed forecastArray
     */
    @NonNull
    private String [] parseForecastJsonStr(String forecastJsonStr) {
        List <String> forecastList;
        try {
            JSONObject forecastObj = new JSONObject(forecastJsonStr);
            JSONArray forecastArray = forecastObj.getJSONArray("list");
            int count = forecastObj.getInt("cnt");
            forecastList = new ArrayList<>(count);
            for (int i = 0; i < count; ++i) {
                JSONObject dailyForecast = (JSONObject) forecastArray.get(i);
                JSONObject temperature = dailyForecast.getJSONObject("temp");
                JSONObject weather = (JSONObject) dailyForecast.getJSONArray("weather").get(0);
                String weatherStr = weather.getString("description") + " - "
                        + temperature.getString("max") + "/" + temperature.getString("min");
                forecastList.add(weatherStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new String[0];
        }
        return forecastList.toArray(new String [] {""});
    }

    private class FetchWeatherTask extends AsyncTask <String, Void, String> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private final String FORECAST_BASE_URL =
                "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private final String QUERY_PARAM = "q";
        private final String FORMAT_PARAM = "mode";
        private final String UNITS_PARAM = "units";
        private final String DAYS_PARAM = "cnt";
        private final String APPID_PARAM = "appid";

        @Override
        protected String doInBackground(String... params) {
            // Url parameters
            String format = "json";
            String units = "metric";
            int numDays = 7;

            // Use Uri.Builder to build an url with parameters
            String url = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
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
                    return response.body().string();
                } else {
                    Log.e(LOG_TAG, "Http request failed.");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Log.e(LOG_TAG, e.toString());
                return null;
            } finally {
                if (response != null)
                    response.close();
            }
        }

        @Override
        protected void onPostExecute(String forecastJsonStr) {
            mForecastAdapter = new ArrayAdapter <>(
                    getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forcast_textview,
                    parseForecastJsonStr(forecastJsonStr)
            );
            mForecastListView.setAdapter(mForecastAdapter);
        }
    }
}
