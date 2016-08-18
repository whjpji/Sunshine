package com.whjpji.sunshine;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

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
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

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
        mForecastAdapter = new ArrayAdapter <>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forcast_textview,
                weakForecast
        );
        mForecastListView = (ListView) layout.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);

        return layout;
    }

    private class FetchWeatherTask extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }
}
