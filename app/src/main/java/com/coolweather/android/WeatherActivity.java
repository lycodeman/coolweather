package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.DailyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utilty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    TextView titleCity;
    TextView titleUpdateTime;
    TextView weatherInfoText;
    LinearLayout forecastLayout;
    TextView confortText;
    TextView carWashText;
    TextView sportText;
    ScrollView weatherLayout;
    TextView dateText;
    TextView infoText;
    TextView maxText;
    TextView minText;
    TextView degreeText;
    TextView aqiText;
    TextView pm25Text;
    private ImageView imageViewBackground;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);


        }
        setContentView(R.layout.activity_weather);
//        ButterKnife.inject(this);
        initView();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        Log.e(TAG, "request: ");
        if (weatherString != null) {
            Weather weather = Utilty.handleWeatherResponse(weatherString);
            mWeatherId=weather.getBasic().getId();
            showWeatherInfo(weather);
        } else {
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            Log.e(TAG, "request: " + weatherId);
            weatherId=getIntent().getStringExtra("weather_id");
            request(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                request(mWeatherId);
            }
        });
        String bingPic=preferences.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(imageViewBackground);
        }else {
            loadBingPic();
        }

    }

    private void loadBingPic() {
        final String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor= (SharedPreferences.Editor)
                        PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(imageViewBackground);
                    }
                });

            }
        });
    }

    private void initView() {

        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        confortText = (TextView) findViewById(R.id.confort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        degreeText = (TextView) findViewById(R.id.degree_text);
        imageViewBackground = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout= (DrawerLayout) findViewById(R.id.draw_layout);
        button= (Button) findViewById(R.id.nav_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private static final String TAG = "WeatherActivity";

    public void request(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=dc918c185cf6471ca80a571edd569224";
        Log.e(TAG, "request: " + weatherId);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utilty.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (weather != null && "ok".equals(weather.getStatus())) {
                            Log.e(TAG, "run: "+weather.toString() );
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败...", Toast.LENGTH_LONG).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        });

    }

    private void showWeatherInfo(Weather weather) {

        String cityName = weather.getBasic().getCity();
        String updateTime = weather.getBasic().getUpdate().getLoc().split(" ")[1];
        String degree = weather.getNow().getTmp() + "℃";
        String weatherInfo = weather.getNow().getCond().getTxt();
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Weather.DailyForecastBean dailyForecast : weather.getDaily_forecast()) {
            Log.e(TAG, "showWeatherInfo: "+dailyForecast.toString() );
            View view = LayoutInflater.from(this).
                    inflate(R.layout.forecast_item, forecastLayout, false);
            dateText = (TextView) view.findViewById(R.id.date_text);
            infoText = (TextView) view.findViewById(R.id.info_text);
            maxText = (TextView) view.findViewById(R.id.max_text);
            minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(dailyForecast.getDate());
            infoText.setText(dailyForecast.getCond().getTxt_d());
            maxText.setText(dailyForecast.getTmp().getMax());
            minText.setText(dailyForecast.getTmp().getMin());
            forecastLayout.addView(view);
        }
        if (weather.getAqi() != null&&weather.getAqi().getCity() != null) {
            Log.e(TAG, "showWeatherInfo: "+weather.getAqi().getCity().getAqi() );
//            aqiText = (TextView) findViewById(R.id.aqi_text);
//            pm25Text = (TextView) findViewById(R.id.pm25_text);
            aqiText.setText(weather.getAqi().getCity().getAqi());
            pm25Text.setText(weather.getAqi().getCity().getPm25());
        }
        String comfort = "舒适度：" + weather.getSuggestion().getComf().getTxt();
        String carWash = "洗车指数：" + weather.getSuggestion().getComf().getTxt();
        String sport = "运动建议：" + weather.getSuggestion().getComf().getTxt();
        confortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
