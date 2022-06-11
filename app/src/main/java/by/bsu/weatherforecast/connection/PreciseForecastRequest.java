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

public class PreciseForecastRequest {
    private Context context;
    private boolean isReady = false;
    private WeatherParser parser = new WeatherParser();
    private List<Weather> forecastResult;

    public PreciseForecastRequest(Context context) {
        this.context = context;
    }

    public void getForecastByLocation (int location) {
        isReady = false;
        String URL = "https://secure-depths-21447.herokuapp.com/location/" + location + "/";
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

    public boolean isReady() {
        return isReady;
    }

    public List<Weather> getForecastResult() {
        return forecastResult;
    }
}
