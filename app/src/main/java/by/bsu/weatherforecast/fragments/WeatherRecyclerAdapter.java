package by.bsu.weatherforecast.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import by.bsu.weatherforecast.R;
import by.bsu.weatherforecast.entity.Weather;
import by.bsu.weatherforecast.entity.WeatherViewHolder;
import by.bsu.weatherforecast.utils.UnitParser;


public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherViewHolder> {
    private List<Weather> itemList;

    public WeatherRecyclerAdapter(List<Weather> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(by.bsu.weatherforecast.R.layout.list_row, viewGroup, false);
        return new WeatherViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder customViewHolder, int i) {
        if (i < 0 || i >= itemList.size())
            return;

        Context context = customViewHolder.itemView.getContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        Weather weatherItem = itemList.get(i);
        Toast.makeText(context, weatherItem.toString(), Toast.LENGTH_SHORT).show();


        String rainString = UnitParser.getRainString(weatherItem.getRain(), weatherItem.getChanceOfPrecipitation());

        TimeZone tz = TimeZone.getDefault();
        String dateString;
        try {
            SimpleDateFormat resultFormat = new SimpleDateFormat("E dd.MM.yyyy - HH:mm");
            resultFormat.setTimeZone(tz);
            dateString = resultFormat.format(weatherItem.getDate());
        } catch (IllegalArgumentException e) {
            dateString = "Error";
        }

        Date now = new Date();
        int color;
        if (weatherItem.getNumDaysFrom(now) > 1) {
            TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.colorTintedBackground, R.attr.colorBackground});
            if (weatherItem.getNumDaysFrom(now) % 2 == 1) {
                color = ta.getColor(0, context.getResources().getColor(R.color.colorTintedBackground));
            } else {
                color = ta.getColor(1, context.getResources().getColor(R.color.colorBackground));
            }
            ta.recycle();
            customViewHolder.itemView.setBackgroundColor(color);
        }
        customViewHolder.itemTemperature.setText(new DecimalFormat("0.#").format(weatherItem.getTemperature()) + " °C");
        customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                weatherItem.getDescription().substring(1) + rainString);

        customViewHolder.itemIcon.setText(UnitParser.getWeatherIconAsText(weatherItem.getWeatherId(), UnitParser.isDayTime(weatherItem, Calendar.getInstance()), context));

        customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " + UnitParser.getName((int) weatherItem.getWind(), context) + (weatherItem.isWindDirectionAvailable() ? " " + UnitParser.getWindDirectionString(context, weatherItem) : ""));
        customViewHolder.itemDate.setText(weatherItem.getDate().toString());
        customViewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + new DecimalFormat("0.0").format(weatherItem.getPressure()) + " hPa/mBar");
        customViewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }

}
