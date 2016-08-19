package com.whjpji.sunshine;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
    ArrayAdapter <String> mForecastAdapter;
    // A list view of weather forecast.
    ListView mForecastListView;

    public ForecastFragment() {
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
        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?" +
                "cnt=7&q=94043&units=metric&mode=json&appid=" +
                BuildConfig.OPEN_WEATHER_MAP_API_KEY;
        new FetchWeatherTask().execute(url);

        return layout;
    }

    /** Parses the json string response
     * @param forecastJsonStr json string of the forecast information.
     * @return parsed forecastArray
     */
    @NonNull
    private String [] parseForecastJsonStr(String forecastJsonStr) {
        List <String> forecastList = null;
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
        return forecastList.toArray(new String [0]);
    }

    private class FetchWeatherTask extends AsyncTask <String, Void, String> {
        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... urls) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(urls[0]).get().build();
            Response response = null;
            try {
                response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    Log.e(TAG, "Http request failed.");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Log.e(TAG, e.toString());
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
