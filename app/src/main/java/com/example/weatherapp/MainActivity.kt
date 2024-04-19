package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.adapter.FragmentPagedAdapter
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.databinding.FragmentBasicWeatherBinding
import com.example.weatherapp.fragments.AdvanceWeatherFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val binding2: FragmentBasicWeatherBinding by lazy {
        FragmentBasicWeatherBinding.inflate(layoutInflater)
    }
    private lateinit var  viewPager2: ViewPager2
    private lateinit var  tabLayout: TabLayout
    private lateinit var adapter: FragmentPagedAdapter
    val advFragment = AdvanceWeatherFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
//        viewPager.adapter = FragmentAdapter(this)


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        tabLayout = binding.tabLayout2
        viewPager2 = binding.viewPager
        adapter = FragmentPagedAdapter(this)
        viewPager2.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

        sharedPreferences = getSharedPreferences(
            "WeatherAppPrefs",
            Context.MODE_PRIVATE
        )
        val units = sharedPreferences.getString("units", "metric") ?: "metric"
        fetachWeatherData("Warsaw", units)
    }

    private fun fetachWeatherData(cityName:String, units:String) {
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("https://api.openweathermap.org/data/2.5/").build().create(
            ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName,"pl","bd04d1ce49301ed0175976c62138cd19",units)
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    val weatherData = WeatherData(
                        temperature = responseBody.main.temp.toString(),
                        humidity = responseBody.main.humidity.toString(),
                        windSpeed = responseBody.wind.speed.toString(),
                        windDeg = responseBody.wind.deg.toString(),
                        sunRise = responseBody.sys.sunrise.toLong(),
                        sunSet = responseBody.sys.sunset.toLong(),
                        pressure = responseBody.main.pressure.toString(),
                        condition = responseBody.weather.firstOrNull()?.main ?: "unknown",
                        maxTemp = responseBody.main.temp_max.toString(),
                        minTemp = responseBody.main.temp_min.toString(),
                        desc = responseBody.weather.first().description,
                        coordinates1 = String.format(Locale.getDefault(), "%.2f", responseBody.coord.lat.toString().toDouble()),
                        coordinates2 = String.format(Locale.getDefault(), "%.2f", responseBody.coord.lon.toString().toDouble())
                    )

                    val gson = Gson()
                    val json = gson.toJson(weatherData)

                    try {
                        val fos = openFileOutput("weather_data.json", Context.MODE_PRIVATE)
                        fos.write(json.toByteArray())
                        fos.close()
                        Log.d("MainActivity", "Data saved to file")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }



                    changeImagsAccordingToWeatherCondition(weatherData.condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                t.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun fetchNextDaysWeatherData(cityName:String, units:String){
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("https://api.openweathermap.org/data/2.5/").build().create(
            ApiInterface::class.java)
        val response = retrofit.getNextDaysWeatherData(cityName,"bd04d1ce49301ed0175976c62138cd19",units)
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    //val tmp = responseBody.
                }
            }
            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                t.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun changeImagsAccordingToWeatherCondition(conditions: String){
        when(conditions){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
            }
        }
    }

}