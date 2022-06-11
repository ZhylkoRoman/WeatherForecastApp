package by.bsu.weatherforecast.connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import by.bsu.weatherforecast.entity.Weather;

public class WeatherParser {

    public static Weather parseWeather(JSONObject response) throws JSONException {
        Weather weather = new Weather();
        String city = response.getString("name");
        JSONObject sys = response.getJSONObject("sys");
        String country = sys.getString("country");
        Long sunset = sys.getLong("sunset");
        Long sunrise = sys.getLong("sunrise");
        Long time = response.getLong("dt");
        JSONObject wind = response.getJSONObject("wind");
        int deg = wind.getInt("deg");
        double speed = wind.getDouble("speed");
        JSONObject main = response.getJSONObject("main");
        double temp = main.getDouble("temp") - 273.15;
        int pressure = main.getInt("pressure");
        int humidity = main.getInt("humidity");
        JSONArray array = response.getJSONArray("weather");
        String descr = array.getJSONObject(0).getString("description");
        int id = array.getJSONObject(0).getInt("id");
        JSONObject coord = response.getJSONObject("coord");
        double lon = coord.getDouble("lon");
        double lat = coord.getDouble("lat");
        final double[] uvi = {0d};
        int city_id = response.getInt("id");

        weather.setCity(city);
        weather.setCountry(country);
        weather.setSunrise(new Date(sunrise * 1000));
        weather.setSunset(new Date(sunset * 1000));
        weather.setLastUpdated(time);
        weather.setWindDirectionDegree((double) deg);
        weather.setWind(speed);
        weather.setTemperature(temp);
        weather.setPressure(pressure);
        weather.setHumidity(humidity);
        weather.setDescription(descr);
        weather.setLat(lat);
        weather.setLon(lon);
        weather.setWeatherId(id);
        weather.setCityId(city_id);
        return weather;
    }

    public static List<Weather> parseForecast(JSONObject response) throws JSONException {
        List<Weather> forecastData = new ArrayList<>();
        JSONArray array = response.getJSONArray("list");
        int cnt = response.getInt("cnt");
        for (int i = 0; i < cnt; ++i) {
            JSONObject item = array.getJSONObject(i);
            Weather weather = new Weather();
            JSONObject main = item.getJSONObject("main");
            double temp = main.getDouble("temp") - 273.15;
            int pressure = main.getInt("pressure");
            int humidity = main.getInt("humidity");
            JSONArray array2 = item.getJSONArray("weather");
            String descr = array2.getJSONObject(0).getString("description");
            int id = array2.getJSONObject(0).getInt("id");
            JSONObject wind = item.getJSONObject("wind");
            int deg = wind.getInt("deg");
            double speed = wind.getDouble("speed");
            long time = item.getLong("dt");

            weather.setLastUpdated(time);
            weather.setWindDirectionDegree((double) deg);
            weather.setWind(speed);
            weather.setTemperature(temp);
            weather.setPressure(pressure);
            weather.setDate(new Date(time * 1000));
            weather.setHumidity(humidity);
            weather.setDescription(descr);
            weather.setWeatherId(id);
            forecastData.add(weather);
        }
        return forecastData;
    }

    public static double parseUvi(JSONObject response) throws JSONException {
        return response.getDouble("value");
    }
}
