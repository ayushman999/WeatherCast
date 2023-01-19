package com.ayushman999.weathercast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private GpsTracker gpsTracker;
    private String LAT, LONG;
    private TextView city, latitude, longitude, temperature, weather,online,offline;
    private TextView temp_NY, temp_Sgp, temp_Mmb, temp_Dlh, temp_Syd, temp_Mlb;
    private TextView wth_NY, wth_Sgp, wth_Mmb, wth_Dlh, wth_Syd, wth_Mlb;
    private Button refresh;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForLocationPermission();

        city = (TextView) findViewById(R.id.tv_city);
        latitude = (TextView) findViewById(R.id.tv_latitude);
        longitude = (TextView) findViewById(R.id.tv_longitude);
        temperature = (TextView) findViewById(R.id.tv_temperature);
        weather = (TextView) findViewById(R.id.tv_weather);
        online = (TextView) findViewById(R.id.online);
        offline = (TextView) findViewById(R.id.offline);
        online.setVisibility(View.INVISIBLE);

        refresh =(Button) findViewById(R.id.btn_refresh);

        temp_NY = (TextView) findViewById(R.id.temp_NY);
        temp_Sgp = (TextView) findViewById(R.id.temp_Sgp);
        temp_Mmb = (TextView) findViewById(R.id.temp_Mmb);
        temp_Dlh = (TextView) findViewById(R.id.temp_Dlh);
        temp_Syd = (TextView) findViewById(R.id.temp_Syd);
        temp_Mlb = (TextView) findViewById(R.id.temp_Mlb);

        wth_NY = (TextView) findViewById(R.id.wth_NY);
        wth_Sgp = (TextView) findViewById(R.id.wth_Sgp);
        wth_Mmb = (TextView) findViewById(R.id.wth_Mmb);
        wth_Dlh = (TextView) findViewById(R.id.wth_Dlh);
        wth_Syd = (TextView) findViewById(R.id.wth_Syd);
        wth_Mlb = (TextView) findViewById(R.id.wth_Mlb);

        refresh.setOnClickListener(view -> {
            refresh();
        });

        refresh();


    }

    private void refresh() {
        getLocation();
        getTopSix();
    }


    private void showSavePref() {
        SharedPreferences sh = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);


        String data = sh.getString("data", "NA:NA:NA:NA:NA");
        String[] values = data.split(":");
        city.setText(values[0]);
        weather.setText(values[1]);
        latitude.setText(values[2]);
        longitude.setText(values[3]);
        temperature.setText(values[4]);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refresh();
    }


    private void getTopSix() {
        setWeather(temp_NY, wth_NY, "New York");
        setWeather(temp_Sgp, wth_Sgp, "Singapore");
        setWeather(temp_Mmb, wth_Mmb, "Mumbai");
        setWeather(temp_Dlh, wth_Dlh, "Delhi");
        setWeather(temp_Syd, wth_Syd, "Sydney");
        setWeather(temp_Mlb, wth_Mlb, "Melbourne");

    }

    private void setWeather(TextView tv_temperature, TextView tv_weather, String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=2f691da0ee7357ba977002ca682601a3";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject main = response.getJSONObject("main");
                            JSONArray wtr = response.getJSONArray("weather");
                            JSONObject wtrObj = wtr.getJSONObject(0);
                            String weather = wtrObj.getString("main");
                            double temp = main.getDouble("temp");
                            double t = temp - 273.15;
                            tv_weather.setText(weather);
                            tv_temperature.setText(String.format("%.2f", t) + "°C");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

    private void getWeather() {

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + LAT + "&lon=" + LONG + "&appid=2f691da0ee7357ba977002ca682601a3";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String name_t = response.getString("name");
                            JSONObject main = response.getJSONObject("main");
                            double temp = main.getDouble("temp");
                            JSONArray wtr = response.getJSONArray("weather");
                            JSONObject wtrObj = wtr.getJSONObject(0);
                            String txt_weather = wtrObj.getString("main");
                            city.setText(name_t);
                            double t = temp - 273.15;
                            temperature.setText(String.format("%.2f", t) + "°C");
                            weather.setText(txt_weather);
                            saveData(name_t, txt_weather, LAT, LONG, String.format("%.2f", t));
                            setOnline();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        showSavePref();
                        setOffline();

                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void saveData(String name_t, String txt_weather, String lat, String aLong, String t) {
        String value = name_t + ":" + txt_weather + ":" + lat + ":" + aLong + ":" + t;
        Toast.makeText(this, "saved:"+value, Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("data", value);
        myEdit.apply();
    }


    private void getLocation() {
        gpsTracker = new GpsTracker(MainActivity.this);
        if (gpsTracker.canGetLocation()) {
            double lt = gpsTracker.getLatitude();
            double lg = gpsTracker.getLongitude();
            LAT = String.format("%.2f", lt);
            LONG = String.format("%.2f", lg);

            if(LAT==null || LAT.equals("0.00")) {
                getLocation();
                return;
            }
            latitude.setText(LAT);
            longitude.setText(LONG);
            getWeather();
        } else {
            gpsTracker.showSettingsAlert();
            showSavePref();
            setOffline();
        }
    }

    private void setOnline(){
        online.setVisibility(View.VISIBLE);
        offline.setVisibility(View.INVISIBLE);
    }

    private void setOffline(){
        online.setVisibility(View.INVISIBLE);
        offline.setVisibility(View.VISIBLE);
    }

    private void askForLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}