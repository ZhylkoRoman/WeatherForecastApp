package by.bsu.weatherforecast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import by.bsu.weatherforecast.adapters.ViewPagerAdapter;
import by.bsu.weatherforecast.connection.PreciseForecastRequest;
import by.bsu.weatherforecast.connection.RequestMaker;
import by.bsu.weatherforecast.databinding.ActivityMainBinding;
import by.bsu.weatherforecast.entity.LongTermWeatherList;
import by.bsu.weatherforecast.entity.Weather;
import by.bsu.weatherforecast.fragments.RecyclerViewFragment;
import by.bsu.weatherforecast.fragments.WeatherRecyclerAdapter;
import by.bsu.weatherforecast.utils.UnitParser;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private final LongTermWeatherList longTermWeatherList = new LongTermWeatherList();
    private TextView todayTemperature;
    private TextView todayDescription;
    private TextView todayWind;
    private TextView todayPressure;
    private TextView todayHumidity;
    private TextView todaySunrise;
    private TextView todaySunset;
    private TextView todayUvIndex;
    private TextView lastUpdate;
    private TextView todayIcon;
    private TextView tapGraph;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityMainBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        todayTemperature = binding.todayTemperature;
        todayDescription = binding.todayDescription;
        todayWind = binding.todayWind;
        todayPressure = binding.todayPressure;
        todayHumidity = binding.todayHumidity;
        todaySunrise = binding.todayHumidity;
        todaySunset = binding.todaySunset;
        todayUvIndex = binding.todayUvIndex;
        lastUpdate = binding.lastUpdate;
        todayIcon = binding.todayIcon;
        tapGraph = binding.tapGraph;
        viewPager = binding.viewPager;
        tabLayout = binding.tabs;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        progressDialog = new ProgressDialog(MainActivity.this);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefreshLayout.setRefreshing(false));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));

        getCityByLocation();

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                swipeRefreshLayout.setEnabled(verticalOffset == 0);
            }
        });
    }

    private void startUpdateById(int id) {
        PreciseForecastRequest preciseForecastRequest = new PreciseForecastRequest(this);
        preciseForecastRequest.getForecastByLocation(id);
        final Weather[] weather = {new Weather()};
        RequestMaker maker = new RequestMaker(this);

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @SuppressLint("ResourceType")
            @Override
            public void handleMessage(Message msg) {
                updateWeather(weather[0]);
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                maker.getWeather(id);
                while (!maker.isReady()) {
                    synchronized (this) {
                        try {
                            wait(100);
                        } catch (Exception e) {
                        }
                    }
                }
                weather[0] = maker.weatherResult();
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }

    private void startUpdateByLatLon(String lat, String lon) {
        final Weather[] weather = {new Weather()};
        RequestMaker maker = new RequestMaker(this);

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @SuppressLint("ResourceType")
            @Override
            public void handleMessage(Message msg) {
                updateWeather(weather[0]);
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                maker.getWeather(lat, lon);
                while (!maker.isReady()) {
                    synchronized (this) {
                        try {
                            wait(100);
                        } catch (Exception e) {
                        }
                    }
                }
                weather[0] = maker.weatherResult();
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }

    private void startUpdateByCity(String city) {
        final Weather[] weather = {new Weather()};
        RequestMaker maker = new RequestMaker(this);

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @SuppressLint("ResourceType")
            @Override
            public void handleMessage(Message msg) {
                updateWeather(weather[0]);
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                maker.getWeather(city);
                while (!maker.isReady()) {
                    synchronized (this) {
                        try {
                            wait(100);
                        } catch (Exception e) {
                        }
                    }
                }
                weather[0] = maker.weatherResult();
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }

    @SuppressLint("SetTextI18n")
    private void updateWeather(Weather weather) {
        String city = weather.getCity();
        String country = weather.getCountry();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(city + (country.isEmpty() ? "" : ", " + country));
        }

        String rainString = UnitParser.getRainString(weather.getRain(), weather.getChanceOfPrecipitation());

        todayTemperature.setText(new DecimalFormat("0.#").format(weather.getTemperature()) + " °C");
        todayDescription.setText(weather.getDescription().substring(0, 1).toUpperCase() +  weather.getDescription().substring(1) + rainString);
        todayWind.setText(getString(R.string.wind) + ": " + UnitParser.getName((int) weather.getWind(), this) + (weather.isWindDirectionAvailable() ? " " + UnitParser.getWindDirectionString(this, weather) : ""));
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("0.0").format(weather.getPressure()) + " hPa/mBar");
        todayHumidity.setText(getString(R.string.humidity) + ": " + weather.getHumidity() + " %");
        todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(weather.getSunrise()));
        todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(weather.getSunset()));
        lastUpdate.setText(new Date(weather.getLastUpdated() * 1000).toString());
        todayIcon.setText(UnitParser.getWeatherIconAsText(weather.getWeatherId(), UnitParser.isDayTime(weather, Calendar.getInstance()), this));

        final double[] uvi = {0};
        RequestMaker maker = new RequestMaker(this);

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @SuppressLint("ResourceType")
            @Override
            public void handleMessage(Message msg) {
                updateUvi(uvi[0], weather.getCityId());
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                maker.getUvi(weather.getLat()+"", weather.getLon()+"");
                while (!maker.isReady()) {
                    synchronized (this) {
                        try {
                            wait(100);
                        } catch (Exception e) {
                        }
                    }
                }
                uvi[0] = maker.uviResult();
                handler.sendEmptyMessage(0);
            }
        });

        thread.start();
    }

    @SuppressLint("SetTextI18n")
    private void updateUvi(double uvi, int id) {
        todayUvIndex.setText("UVindex: " + uvi);

        final List<Weather>[] forecast = new List[]{null};
        RequestMaker maker = new RequestMaker(this);
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @SuppressLint("ResourceType")
            @Override
            public void handleMessage(Message msg) {
                updateForecast(forecast[0]);
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                maker.getForecast(id);
                while (!maker.isReady()) {
                    synchronized (this) {
                        try {
                            wait(100);
                        } catch (Exception e) {
                        }
                    }
                }
                forecast[0] = maker.forecastResult();
                handler.sendEmptyMessage(0);
            }
        });

        thread.start();
    }

    private void updateForecast(List<Weather> forecast) {
        longTermWeatherList.clear();
        longTermWeatherList.addAll(forecast);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (currentPage == 0 && longTermWeatherList.getToday().isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(longTermWeatherList.getToday());
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(longTermWeatherList.getTomorrow());
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(longTermWeatherList.getLater());
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            //refreshWeather();
            return true;
        }
        if (id == R.id.action_search) {
            searchCities();
            return true;
        }
        if (id == R.id.action_location) {
            getCityByLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            lastUpdate.setText("");
        } else {
            Calendar lastCheckedCal = new GregorianCalendar();
            lastCheckedCal.setTimeInMillis(timeInMillis);
            Date lastCheckedDate = new Date(timeInMillis);
            String timeFormat = android.text.format.DateFormat.getTimeFormat(this).format(lastCheckedDate);
            lastUpdate.setText(timeFormat);
        }
    }


    private void searchCities() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);

        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setPadding(32, 0, 32, 0);
        inputLayout.addView(input);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Search city");
        alert.setView(inputLayout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString().trim();
                if (!result.isEmpty()) {
                    startUpdateByCity(result);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    void getCityByLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Getting location");
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        locationManager.removeUpdates(MainActivity.this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();
            Location location = null;
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && location == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location != null) {
                Toast.makeText(this, location.getLatitude() + "  " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
                startUpdateByLatLon(location.getLatitude()+"", location.getLongitude()+"");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}