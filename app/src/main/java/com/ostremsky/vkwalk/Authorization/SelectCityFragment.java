package com.ostremsky.vkwalk.Authorization;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ostremsky.vkwalk.InfoHelper;
import com.ostremsky.vkwalk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DevAs on 03.07.2016.
 */
public class SelectCityFragment extends Fragment {
    public ListView citiesListView;
    private List<City> cities;
    private ArrayAdapter<City> adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cities = new ArrayList<>();
        View v = inflater.inflate(R.layout.fragment_select_city, container, false);
        citiesListView = (ListView) v.findViewById(R.id.citiesListView);
        adapter = new Adapter(cities);
        citiesListView.setAdapter(adapter);
        getCities();
        return v;
    }
    public void getCities(){
        String url = Uri.parse(InfoHelper.URL_I).buildUpon()
                .appendQueryParameter(InfoHelper.GET_METHOD, "getCities")
                .build().toString();
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    try {

                        JSONArray array = new JSONArray(response.body().string());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            String name = object.getString("name");
                            City city = new City(object.getInt("id"), name, object.getString("group_id"));
                            cities.add(city);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("cities", String.valueOf(cities.size()));
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }
    public void addUserToDb(int cityId){
        int userId = InfoHelper.getUserId();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(InfoHelper.URL_I).newBuilder();
        urlBuilder.addQueryParameter(InfoHelper.GET_METHOD, "insertUser");
        urlBuilder.addQueryParameter("vk_id", String.valueOf(userId));
        urlBuilder.addQueryParameter("city", String.valueOf(cityId));
        String url = urlBuilder.build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                }
            }
        });
    }
    class Adapter extends ArrayAdapter<City>{

        public Adapter(List<City> cities) {
            super(getActivity(), android.R.layout.simple_list_item_1, cities);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final City curCity = cities.get(position);
            if(v == null){
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = (TextView) v.findViewById(android.R.id.text1);
            textView.setText(curCity.name);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addUserToDb(curCity.id);
                    ((AuthorizeActivity) getActivity()).checkSubscribe();
                }
            });
            return v;
        }
    }
}