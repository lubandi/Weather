package com.a5corp.weather;

import android.support.annotation.NonNull;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.a5corp.weather.model.CitySearch;
import com.a5corp.weather.model.Weather;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {
    Typeface weatherFont;
    Button button;
    TextView detailsField[] = new TextView[10] , weatherIcon[] = new TextView[11];
    TextView windView , humidityView , directionView, dailyView, updatedField, cityField;
    double tc;
    Handler handler;
    JSONObject json0 , json1;
    int Clicks = 0;
    MaterialDialog pd;
    Weather mWeather;
    CitySearch mCitySearch;
    ConnectionDetector mConnectionDetector;
    boolean isNetworkAvailable;

    private void updateWeatherData(final String city) {
        new Thread(){
            public void run(){
                final JSONObject[] json = RemoteFetch.getJSON(getActivity(), city);
                if(json == null || !isNetworkAvailable) {
                    GlobalActivity.i = -1;
                    GlobalActivity.cp.setCity(GlobalActivity.cp.getLastCity());
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                            if (GlobalActivity.cp.getLaunched()) {
                                pd.dismiss();
                                Intent intent = new Intent(getActivity(), FirstLaunch.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Log.i("Loaded" , "Weather");
                                startActivity(intent);
                            }
                            else {
                                pd.dismiss();
                                showInputDialog();
                            }
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            GlobalActivity.cp.setLaunched();
                            renderWeather(json);
                            pd.dismiss();
                            GlobalActivity.cp.setLastCity(city);
                        }
                    });
                }
            }
        }.start();
    }

    public void Units(JSONObject json1)
    {
        try {
            int bool = Clicks % 2;
            switch (bool) {
                case 0 :
                    double Fah = json1.getJSONObject("main").getDouble("temp") * 1.8 + 32;
                    int F = (int) Fah;
                    String result = Integer.toString(F) + "°F";
                    button.setText(result);
                    ++Clicks;
                    break;
                case 1:
                    result = (int) Math.round(json1.getJSONObject("main").getDouble("temp")) + "°C";
                    button.setText(result);
                    ++Clicks;
                    break;
            }
        }
        catch (Exception ex)
        {
            Log.e("Unlikely" , "Why?");
        }
    }

    public void changeCity(String city)
    {
        pd.show();
        updateWeatherData(city);
        GlobalActivity.cp.setCity(city);
    }

    private void renderWeather(JSONObject[] jsonObj){
        try {
            button.setVisibility(View.INVISIBLE);
            Clicks = 0;
            Log.i("Showed" , "Done");
            json0 = jsonObj[0];
            json1 = jsonObj[1];
            tc = json1.getJSONObject("main").getDouble("temp");
            int a = (int) Math.round(json1.getJSONObject("main").getDouble("temp"));                        //℃
            cityField.setText(json0.getJSONObject("city").getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json0.getJSONObject("city").getString("country"));
            cityField.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v) {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                            .title("City Information")
                            .content("Loading")
                            .positiveText("OK");
                    try {
                        builder.content(json0.getJSONObject("city").getString("name").toUpperCase(Locale.US) +
                                ", " +
                                json0.getJSONObject("city").getString("country"));
                        MaterialDialog dialog = builder.build();
                        dialog.show();
                        Log.i("Loaded in Dialog", "City Name");
                    } catch (Exception ex) {
                        Log.e("Error", "Could not load city name");
                    }
                }
            });
            Log.i("Location" , "Location Received");
            JSONObject details[] = new JSONObject[10];
            for (int i = 0; i < 10; ++i)
            {
                details[i] = json0.getJSONArray("list").getJSONObject(i);
            }
            Log.i("Objects" , "JSON Objects Created");
            for (int i = 0; i < 10; ++i)
            {
                final JSONObject J = details[i];
                String date1 = details[i].getString("dt");
                Date expiry = new Date(Long.parseLong(date1) * 1000);
                String date = new SimpleDateFormat("EE, dd" , Locale.US).format(expiry);
                SpannableString ss1=  new SpannableString(date + "\n"
                + details[i].getJSONObject("temp").getLong("max") + "°" + "      "
                + details[i].getJSONObject("temp").getLong("min") + "°" + "\n");
                ss1.setSpan(new RelativeSizeSpan(1.1f), 0,7, 0); // set size
                ss1.setSpan(new RelativeSizeSpan(1.4f) , 8 , 11 , 0);
                detailsField[i].setText(ss1);
                Log.i("Details[" + Integer.toString(i) + "]", "Information String " + Integer.toString(i + 1) + " loaded");
                setWeatherIcon(details[i].getJSONArray("weather").getJSONObject(0).getInt("id") , i);
                detailsField[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                                .title("Weather Information")
                                .content("Loading")
                                .positiveText("OK");
                        try {
                            String date1 = J.getString("dt");
                            Date expiry = new Date(Long.parseLong(date1) * 1000);
                            String date = new SimpleDateFormat("EE, dd MMMM yyyy" , Locale.US).format(expiry);
                            builder.content(date +
                                    "\n" + J.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase(Locale.US) +
                                    "\n" + "Maximum: " + J.getJSONObject("temp").getLong("max") + " ℃" +
                                    "\n" + "Minimum:  " + J.getJSONObject("temp").getLong("min") + " ℃" +
                                    "\n" + "Morning:    " + J.getJSONObject("temp").getLong("morn") + " ℃" +
                                    "\n" + "At Night:    " + J.getJSONObject("temp").getLong("night") + " ℃" +
                                    "\n" + "Evening:    " + J.getJSONObject("temp").getLong("eve") + " ℃" +
                                    "\n" + "Humidity:  " + J.getString("humidity") + "%" +
                                    "\n" + "Pressure:  " + J.getString("pressure") + " hPa" +
                                    "\n" + "Wind:         " + J.getString("speed") + "km/h");
                            MaterialDialog dialog = builder.build();
                            dialog.show();
                        Log.i("Loaded" , "Details Field");}
                        catch (Exception e) {
                            Log.e("Error", "Something's wrong in the JSON Received");
                        }
                    }
                });
                weatherIcon[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                                .title("Weather Information")
                                .content("Loading")
                                .positiveText("OK");
                        try {
                            String date1 = J.getString("dt");
                            Date expiry = new Date(Long.parseLong(date1) * 1000);
                            String date = new SimpleDateFormat("EE, dd MMMM yyyy" , Locale.US).format(expiry);
                            builder.content(date +
                                    "\n" + J.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase(Locale.US) +
                                    "\n" + "Maximum: " + J.getJSONObject("temp").getLong("max") + " ℃" +
                                    "\n" + "Minimum:  " + J.getJSONObject("temp").getLong("min") + " ℃" +
                                    "\n" + "Morning:    " + J.getJSONObject("temp").getLong("morn") + " ℃" +
                                    "\n" + "At Night:    " + J.getJSONObject("temp").getLong("night") + " ℃" +
                                    "\n" + "Evening:    " + J.getJSONObject("temp").getLong("eve") + " ℃" +
                                    "\n" + "Humidity:  " + J.getString("humidity") + "%" +
                                    "\n" + "Pressure:  " + J.getString("pressure") + " hPa" +
                                    "\n" + "Wind:         " + J.getString("speed") + "km/h");
                            MaterialDialog dialog = builder.build();
                            dialog.show();
                            Log.i("Loaded" , "Details Field");}
                        catch (Exception e) {
                            Log.e("Error", "Something's wrong in the JSON Received");
                        }
                    }
                });
            }
            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = "Last update: " + df.format(new Date(json1.getLong("dt")*1000));
            updatedField.setText(updatedOn);
            int deg = json1.getJSONObject("wind").getInt("deg");
            if (deg < 90)
                directionView.setText(getActivity().getString(R.string.top_right));
            else if (deg == 90)
                directionView.setText(getActivity().getString(R.string.right));
            else if (deg < 180)
                directionView.setText(getActivity().getString(R.string.bottom_right));
            else if (deg == 180)
                directionView.setText(getActivity().getString(R.string.down));
            else if (deg < 270)
                directionView.setText(getActivity().getString(R.string.bottom_left));
            else if (deg == 270)
                directionView.setText(getActivity().getString(R.string.left));
            else
                directionView.setText(getActivity().getString(R.string.top_left));
            setWeatherIcon(json1.getJSONArray("weather").getJSONObject(0).getInt("id"),10);
            humidityView.setText("HUMIDITY:\n" + json1.getJSONObject("main").getInt("humidity") + "%");
            Log.i("Humidity Loaded" , "Done");
            windView.setText("WIND:\n" + json1.getJSONObject("wind").getDouble("speed") + "km/h");
            Log.i("Wind Loaded" , "Done");
            Log.i("10" , "Weather Icon 11 Set");
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick (View v)
                {
                    Units(json1);
                }
            });
            weatherIcon[10].setOnClickListener(new View.OnClickListener()
            {
                public void onClick (View v)
                {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                            .title("Weather Information")
                            .positiveText("OK");
                    try {
                        String d1 = new java.text.SimpleDateFormat("hh:mm:ss a" , Locale.US).format(new Date(json1.getJSONObject("sys").getLong("sunrise")*1000));
                        String d2 = new java.text.SimpleDateFormat("hh:mm:ss a" , Locale.US).format(new Date(json1.getJSONObject("sys").getLong("sunset")*1000));
                        builder.content(json1.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase(Locale.US) +
                                "\n" + "TEMPERATURE :\t " + json1.getJSONObject("main").getInt("temp") + " ℃" +
                                "\n" + "Maximum:\t " + json1.getJSONObject("main").getDouble("temp_max") + " ℃" +
                                "\n" + "Minimum:\t " + json1.getJSONObject("main").getDouble("temp_min") + " ℃" +
                                "\n" + "Humidity:\t   " + json1.getJSONObject("main").getString("humidity") + "%" +
                                "\n" + "Pressure:\t   " + json1.getJSONObject("main").getString("pressure") + " hPa" +
                                "\n" + "Wind:\t         " + json1.getJSONObject("wind").getString("speed") + "km/h" +
                                "\n" + "Sunrise:\t     " + d1 +
                                "\n" + "Sunset:\t       " + d2);
                        MaterialDialog dialog = builder.build();
                        dialog.show();
                        Log.i("Load" , "Main Weather Icon OnClick Details loaded");
                    }
                    catch (Exception e) {
                        Log.e("Error", "Main Weather Icon OnClick Details could not be loaded");
                    }
                }
            });
            String r1 = Integer.toString(a) + "°C";
            button.setText(r1);
            button.setVisibility(View.VISIBLE);
        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    public WeatherFragment() {
        handler = new Handler();
    }

    private void setWeatherIcon(int id , int i) {
        String icon = "";
        switch(id) {
            case 501 : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 500 : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 502 : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 503 : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 504 : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 511 : icon = getActivity().getString(R.string.weather_rain_wind);
                break;
            case 520 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 521 : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 522 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 531 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 200 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 201 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 202 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 210 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 211 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 212 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 221 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 230 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 231 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 232 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 300 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 301 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 302 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 310 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 311 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 312 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 313 : icon = getActivity().getString(R.string.weather_rain_drizzle);
                break;
            case 314 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 321 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 600 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 601 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 602 : icon = getActivity().getString(R.string.weather_heavy_snow);
                break;
            case 611 : icon = getActivity().getString(R.string.weather_sleet);
                break;
            case 612 : icon = getActivity().getString(R.string.weather_heavy_snow);
                break;
            case 903 :
            case 615 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 616 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 620 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 621 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 622 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 701 :
            case 702 :
            case 721 : icon = getActivity().getString(R.string.weather_smoke);
                break;
            case 751 :
            case 761 :
            case 731 : icon = getActivity().getString(R.string.weather_dust);
                break;
            case 741 : icon = getActivity().getString(R.string.weather_foggy);
                break;
            case 762 : icon = getActivity().getString(R.string.weather_volcano);
                break;
            case 771 :
            case 900 :
            case 781 : icon = getActivity().getString(R.string.weather_tornado);
                break;
            case 904 : icon = getActivity().getString(R.string.weather_sunny);
                break;
            case 800 : icon = getActivity().getString(R.string.weather_sunny);
                break;
            case 801 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 802 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 803 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 804 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 901 : icon = getActivity().getString(R.string.weather_storm);
                break;
            case 902 : icon = getActivity().getString(R.string.weather_hurricane);
                break;
        }
        Log.i(Integer.toString(id) , Integer.toString(i));
        weatherIcon[i].setText(icon);
    }

    private void showInputDialog() {
        new MaterialDialog.Builder(this.getActivity())
                .title("Change City")
                .content("Hey there, could not find the city you wanted. Please enter a new one:")
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog , @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, @NonNull CharSequence input) {
                        changeCity(input.toString());
                    }
                }).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        humidityView = (TextView) rootView.findViewById(R.id.humidity_view);
        windView = (TextView) rootView.findViewById(R.id.wind_view);
        directionView = (TextView)rootView.findViewById(R.id.direction_view);
        directionView.setTypeface(weatherFont);
        dailyView = (TextView)rootView.findViewById(R.id.daily_view);
        dailyView.setText(getString(R.string.daily));
        button = (Button)rootView.findViewById(R.id.button1);
        button.setText("°C");
        pd.show();
        for (int i = 0; i < 11; ++i)
        {
            String f = "details_view" + (i + 1) , g = "weather_icon" + (i + 1);
            if (i != 10) {
                int resID = getResources().getIdentifier(f, "id", getContext().getPackageName());
                detailsField[i] = (TextView) rootView.findViewById(resID);
            }
            int resIDI = getResources().getIdentifier(g, "id" , getContext().getPackageName());
            weatherIcon[i] = (TextView)rootView.findViewById(resIDI);
            weatherIcon[i].setTypeface(weatherFont);
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mWeather = new Weather();
        mCitySearch = new CitySearch();
        mConnectionDetector = new ConnectionDetector(getActivity());
        isNetworkAvailable = mConnectionDetector.isNetworkAvailableAndConnected();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this.getActivity())
                .title("Please Wait")
                .content("Loading")
                .cancelable(false)
                .progress(true , 0);
        pd = builder.build();
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(GlobalActivity.cp.getCity());
    }
}
