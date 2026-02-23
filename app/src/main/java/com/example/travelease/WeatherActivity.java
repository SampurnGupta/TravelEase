package com.example.travelease;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity {
    private EditText etLocation;
    private TextView tvCity, tvTemperature, tvWeatherDesc;
    private ImageView ivWeatherIcon;
    private Button btnGetWeather;
    private final String API_KEY = "8847ab09cec6635e36d8a1d6d3781f3b"; // Your API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);

        etLocation = findViewById(R.id.et_location);
        btnGetWeather = findViewById(R.id.btn_get_weather);
        tvCity = findViewById(R.id.tv_city);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);

        btnGetWeather.setOnClickListener(v -> fetchWeather());
    }

    private void fetchWeather() {
        String city = etLocation.getText().toString().trim();
        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String cityName = response.getString("name");
                        double temp = response.getJSONObject("main").getDouble("temp");
                        String description = response.getJSONArray("weather").getJSONObject(0).getString("description");
                        String iconCode = response.getJSONArray("weather").getJSONObject(0).getString("icon");
                        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                        tvCity.setText(cityName);
                        tvTemperature.setText("Temperature: " + temp + "°C");
                        tvWeatherDesc.setText("Condition: " + description.substring(0, 1).toUpperCase() + description.substring(1));
                        Picasso.get().load(iconUrl).into(ivWeatherIcon);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch weather", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}
