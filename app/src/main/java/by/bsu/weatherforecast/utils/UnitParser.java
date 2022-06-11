package by.bsu.weatherforecast.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import by.bsu.weatherforecast.R;
import by.bsu.weatherforecast.entity.Weather;

public class UnitParser {
    public static String getWindDirectionString(Context context, Weather weather) {
        try {
            if (weather.getWind() != 0)
                return weather.getWindDirection(8).getArrow(context);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isDayTime(Weather W, Calendar Cal){
        Date Sunrise = W.getSunrise();
        Date Sunset = W.getSunset();
        boolean day;
        if((Sunrise != null) && (Sunset != null)){
            Date currentTime = Calendar.getInstance().getTime();    // Cal is always set to midnight
            // then get real time
            day = currentTime.after(W.getSunrise()) && currentTime.before(W.getSunset());
        }
        else{
            // fallback
            int hourOfDay = Cal.get(Calendar.HOUR_OF_DAY);
            day = (hourOfDay >= 7 && hourOfDay < 20);
        }
        return day;
    }

    public static String getWeatherIconAsText(int weatherId, boolean isDay,
                                              @NonNull Context context) {
        int id = weatherId / 100;
        String icon = "";

        if (id == 2) {
            // thunderstorm
            switch (weatherId) {
                case 210:
                case 211:
                case 212:
                case 221:
                    icon = context.getString(R.string.weather_lightning);
                    break;
                case 200:
                case 201:
                case 202:
                case 230:
                case 231:
                case 232:
                default:
                    icon = context.getString(R.string.weather_thunderstorm);
                    break;
            }
        } else if (id == 3) {
            // drizzle/sprinkle
            switch (weatherId) {
                case 302:
                case 311:
                case 312:
                case 314:
                    icon = context.getString(R.string.weather_rain);
                    break;
                case 310:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 313:
                    icon = context.getString(R.string.weather_showers);
                    break;
                case 300:
                case 301:
                case 321:
                default:
                    icon = context.getString(R.string.weather_sprinkle);
                    break;
            }
        } else if (id == 5) {
            // rain
            switch (weatherId) {
                case 500:
                    icon = context.getString(R.string.weather_sprinkle);
                    break;
                case 511:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 520:
                case 521:
                case 522:
                    icon = context.getString(R.string.weather_showers);
                    break;
                case 531:
                    icon = context.getString(R.string.weather_storm_showers);
                    break;
                case 501:
                case 502:
                case 503:
                case 504:
                default:
                    icon = context.getString(R.string.weather_rain);
                    break;
            }
        } else if (id == 6) {
            // snow
            switch (weatherId) {
                case 611:
                    icon = context.getString(R.string.weather_sleet);
                    break;
                case 612:
                case 613:
                case 615:
                case 616:
                case 620:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 600:
                case 601:
                case 602:
                case 621:
                case 622:
                default:
                    icon = context.getString(R.string.weather_snow);
                    break;
            }
        } else if (id == 7) {
            // atmosphere
            switch (weatherId) {
                case 711:
                    icon = context.getString(R.string.weather_smoke);
                    break;
                case 721:
                    icon = context.getString(R.string.weather_day_haze);
                    break;
                case 731:
                case 761:
                case 762:
                    icon = context.getString(R.string.weather_dust);
                    break;
                case 751:
                    icon = context.getString(R.string.weather_sandstorm);
                    break;
                case 771:
                    icon = context.getString(R.string.weather_cloudy_gusts);
                    break;
                case 781:
                    icon = context.getString(R.string.weather_tornado);
                    break;
                case 701:
                case 741:
                default:
                    icon = context.getString(R.string.weather_fog);
                    break;
            }
        } else if (id == 8) {
            // clear sky or cloudy
            switch (weatherId) {
                case 800:
                    icon = isDay
                            ? context.getString(R.string.weather_day_sunny)
                            : context.getString(R.string.weather_night_clear);
                    break;
                case 801:
                case 802:
                    icon = isDay
                            ? context.getString(R.string.weather_day_cloudy)
                            : context.getString(R.string.weather_night_alt_cloudy);
                    break;
                case 803:
                case 804:
                default:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
            }
        } else if (id == 9) {
            switch (weatherId) {
                case 900:
                    icon = context.getString(R.string.weather_tornado);
                    break;
                case 901:
                    icon = context.getString(R.string.weather_storm_showers);
                    break;
                case 902:
                    icon = context.getString(R.string.weather_hurricane);
                    break;
                case 903:
                    icon = context.getString(R.string.weather_snowflake_cold);
                    break;
                case 904:
                    icon = context.getString(R.string.weather_hot);
                    break;
                case 905:
                    icon = context.getString(R.string.weather_windy);
                    break;
                case 906:
                    icon = context.getString(R.string.weather_hail);
                    break;
                case 957:
                default:
                    icon = context.getString(R.string.weather_strong_wind);
                    break;
            }
        }

        return icon;
    }

    public static String getRainString(double rain, double percentOfPrecipitation) {
        StringBuilder sb = new StringBuilder();
        if (rain > 0) {
            sb.append(" (");
            if (rain < 0.1) {
                sb.append("<0.1");
            }
            if (percentOfPrecipitation > 0) {
                sb.append(", ").append(String.format(Locale.ENGLISH, "%d%%", (int) (percentOfPrecipitation * 100)));
            }

            sb.append(")");
        }

        return sb.toString();
    }

    public static String getName(int wind, Context context) {
        if (wind == 0) {
            return context.getString(by.bsu.weatherforecast.R.string.beaufort_calm);
        } else if (wind == 1) {
            return context.getString(R.string.beaufort_light_air);
        } else if (wind == 2) {
            return context.getString(R.string.beaufort_light_breeze);
        } else if (wind == 3) {
            return context.getString(R.string.beaufort_gentle_breeze);
        } else if (wind == 4) {
            return context.getString(R.string.beaufort_moderate_breeze);
        } else if (wind == 5) {
            return context.getString(R.string.beaufort_fresh_breeze);
        } else if (wind == 6) {
            return context.getString(R.string.beaufort_strong_breeze);
        } else if (wind == 7) {
            return context.getString(R.string.beaufort_high_wind);
        } else if (wind == 8) {
            return context.getString(R.string.beaufort_gale);
        } else if (wind == 9) {
            return context.getString(R.string.beaufort_strong_gale);
        } else if (wind == 10) {
            return context.getString(R.string.beaufort_storm);
        } else if (wind == 11) {
            return context.getString(R.string.beaufort_violent_storm);
        } else {
            return context.getString(R.string.beaufort_hurricane);
        }
    }
}
