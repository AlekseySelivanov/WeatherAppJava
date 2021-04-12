package com.example.weatherappjava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "c030482d5f486ff9ef0c6ff7c9de1247";

    Button btnSearch;
    AutoCompleteTextView cityName;
    ImageView iconWeather;
    TextView tvTemp, tvCity;
    ListView dailyWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        btnSearch = findViewById(R.id.btnSearch);
        cityName = findViewById(R.id.cityName);
        iconWeather = findViewById(R.id.iconWeather);
        tvTemp = findViewById(R.id.tvTemp);
        tvCity = findViewById(R.id.tvCity);
        dailyWeather = findViewById(R.id.dailyWeather);


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString();
                if(city.isEmpty())
                    Toast.makeText(MainActivity.this,"Пожалуйста, введите город", Toast.LENGTH_SHORT).show();
                else{
                    // todo: load weather by city name;
                        loadWeatherByCityName(city);
                }
            }
        });
    }

    private void loadWeatherByCityName(String city) {
        String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q="+city+"&&units=metric&appid="+API_KEY+"&lang=ru";
        apiUrl = apiUrl.replace(" ", "");
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if(e != null){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"Ошибка сервера", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            JsonObject main = result.get("main").getAsJsonObject();
                            double temp = main.get("temp").getAsDouble();
                            tvTemp.setText(temp+"°C");

                            JsonObject sys = result.get("sys").getAsJsonObject();
                            String country = sys.get("country").getAsString();
                            tvCity.setText(city + ","+country);

                            JsonArray weather = result.get("weather").getAsJsonArray();
                            String icon = weather.get(0).getAsJsonObject().get("icon").getAsString();
                            loadIcon(icon);

                            JsonObject coord = result.get("coord").getAsJsonObject();
                            double lon = coord.get("lon").getAsDouble();
                            double lat = coord.get("lat").getAsDouble();
                            loadDailyForecast(lon,lat);


                        }
                    }
                });

    }

    private void loadDailyForecast(double lon, double lat) {
        String apiUrl = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=hourly,minutely,current&units=metric&appid="+API_KEY+"&lang=ru";
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                        } else {
                            List<Weather> weatherList = new ArrayList<>();
                            String timeZone = result.get("timezone").getAsString();
                            JsonArray daily = result.get("daily").getAsJsonArray();
                            for(int i=1;i<daily.size();i++){
                                Long date = daily.get(i).getAsJsonObject().get("dt").getAsLong();
                                Double temp = daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("day").getAsDouble();
                                String icon = daily.get(i).getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString();
                                weatherList.add(new Weather(date, timeZone, temp, icon));
                            }
                            DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(MainActivity.this, weatherList);
                            dailyWeather.setAdapter(dailyWeatherAdapter);
                        }
                    }
                });
                }

    private void loadIcon(String icon){
        Ion.with(this)
                .load("http://openweathermap.org/img/w/"+icon+".png")
                .intoImageView(iconWeather);
    }
}