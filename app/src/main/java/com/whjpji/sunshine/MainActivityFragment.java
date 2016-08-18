package com.whjpji.sunshine;

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
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    ArrayAdapter <String> mForcastAdapter;
    ListView mForcastListView;

    final static List<String> FAKE_DATA = Arrays.asList(
            "Today - Sunny - 88/63",
            "Tommorrow - Foggy - 70/46",
            "Weds - Cloudy - 72/63",
            "Thurs - Rainy - 64/51",
            "Fri - Foggy - 70/46",
            "Sat - Sunny - 76/68"
    );

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);

        mForcastAdapter = new ArrayAdapter <>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forcast_textview,
                FAKE_DATA
        );
        mForcastListView = (ListView) layout.findViewById(R.id.listview_forecast);
        mForcastListView.setAdapter(mForcastAdapter);

        return layout;
    }
}
