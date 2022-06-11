package by.bsu.weatherforecast.connection;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import by.bsu.weatherforecast.entity.Weather;

public class RequestMaker {

    private Context context;
    private boolean isReady = false;
    private WeatherParser parser = new WeatherParser();
    private Weather weatherResult;
    private List<Weather> forecastResult;
    private double uviResult;

    public RequestMaker(Context context) {
        this.context = context;
    }

    public void getWeather(String city) {
        getWeatherRequest("q=" + city);
    }

    public void getWeather(String lat, String lon) {
        getWeatherRequest("lat=" + lat + "&lon=" + lon);
    }

    public void getWeather(int id) {
        getWeatherRequest("id=" + id);
    }

    public void getForecast(String city) {
        getForecastRequest("q=" + city);
    }

    public void getForecast(String lat, String lon) {
        getForecastRequest("lat=" + lat + "&lon=" + lon);
    }

    public void getForecast(int id) {
        getForecastRequest("id=" + id);
    }

    public boolean isReady() {
        return isReady;
    }

    private void getWeatherRequest(String place) {
        isReady = false;
        String URL = "https://api.openweathermap.org/data/2.5/weather?" + place + "&lang=en&mode=json&appid=f0d198e1b6b416821fb69eb0228d8dd3";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    weatherResult = parser.parseWeather(response);
                    isReady = true;
                } catch (JSONException e) {
                    Log.e("Error", e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.getLocalizedMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void getForecastRequest(String place) {
        isReady = false;
        String URL = "https://api.openweathermap.org/data/2.5/forecast?" + place + "&lang=en&mode=json&appid=f0d198e1b6b416821fb69eb0228d8dd3";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    forecastResult = parser.parseForecast(response);
                    isReady = true;
                } catch (JSONException e) {
                    Log.e("Error", e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.getLocalizedMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void getUvi(String lat, String lon) {
        isReady = false;
        String URL = "https://api.openweathermap.org/data/2.5/uvi?lat=" + lat + "&lon=" + lon + "&lang=en&mode=json&appid=f0d198e1b6b416821fb69eb0228d8dd3";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    uviResult = parser.parseUvi(response);
                    isReady = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.getLocalizedMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public Weather weatherResult() {
        return weatherResult;
    }

    public double uviResult() {
        return uviResult;
    }

    public List<Weather> forecastResult() {
        return forecastResult;
    }
}
