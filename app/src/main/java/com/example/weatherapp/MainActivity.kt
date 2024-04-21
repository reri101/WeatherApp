package com.example.weatherapp

import SettingsFragment
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.adapter.FragmentPagedAdapter
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.databinding.FragmentBasicWeatherBinding
import com.example.weatherapp.fragments.AdvanceWeatherFragment
import com.example.weatherapp.fragments.FavouriteListFragment
import com.example.weatherapp.fragments.NextDaysWeatherFragment
import com.example.weatherapp.fragments.WeatherFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        sharedPreferences = getSharedPreferences(
            "WeatherAppPrefs",
            Context.MODE_PRIVATE
        )
        val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
        fetachWeatherData("Warsaw", units)

        val fragmentManager: FragmentManager = supportFragmentManager
        val favouriteListFragment = FavouriteListFragment()
        val settingsFragment = SettingsFragment()
        val weatherFragment = WeatherFragment()
        val tabLayout = binding.tabLayout
        val pogodaTab = tabLayout.getTabAt(1)
        pogodaTab?.select()

        replaceFragment(weatherFragment)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if(position==0){
                    replaceFragment(favouriteListFragment)
                }else if(position==1){
                    replaceFragment(weatherFragment)
                }else if(position==2){
                    replaceFragment(settingsFragment)
                } else{
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.constLayout, fragment)
        fragmentTransaction.commit()
    }

    private fun fetachWeatherData(cityName:String, units:String) {
        if (!shouldRefreshWeather()) {
            // Nie odświeżaj danych o pogodzie, jeśli ostatnie odświeżenie było wystarczająco niedawno
            return
        }else {
            val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.openweathermap.org/data/2.5/").build().create(
                ApiInterface::class.java
            )
            val response =
                retrofit.getWeatherData(cityName, "pl", "bd04d1ce49301ed0175976c62138cd19", units)
            response.enqueue(object : Callback<WeatherApp> {
                override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                    val responseBody = response.body()

                    if (response.isSuccessful && responseBody != null) {
                        val weatherData = WeatherData(
                            cityName = cityName,
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
                            coordinates1 = String.format(
                                Locale.getDefault(),
                                "%.2f",
                                responseBody.coord.lat.toString().toDouble()
                            ),
                            coordinates2 = String.format(
                                Locale.getDefault(),
                                "%.2f",
                                responseBody.coord.lon.toString().toDouble()
                            )
                        )

                        val gson = Gson()
                        val json = gson.toJson(weatherData)
                        val cityNameLower= cityName.lowercase()

                        try {
                            val fos = openFileOutput("weather_data_$cityNameLower.json", Context.MODE_PRIVATE)
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
                        Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
        }
    }
    private fun shouldRefreshWeather(): Boolean {
        val lastRefreshTime = sharedPreferences.getLong("lastRefreshTime", 0)
        val refreshFrequency = sharedPreferences.getInt("refreshFrequency", 6) // Domyślna wartość: 6 godzin
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastRefreshTime
        val elapsedTimeHours = elapsedTime / (1000 * 60 * 60) // Przelicz na godziny

        return elapsedTimeHours >= refreshFrequency
    }
    private fun fetchWeatherDataFromFile() {
        try {
            val fis = openFileInput("weather_data.json")
            val inputStreamReader = InputStreamReader(fis)
            val bufferedReader = BufferedReader(inputStreamReader)
            val json = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                json.append(line)
                line = bufferedReader.readLine()
            }
            fis.close()

            val gson = Gson()
            val weatherData = gson.fromJson(json.toString(), WeatherData::class.java)

            // Wyświetl dane o pogodzie z pliku JSON
            //displayWeatherData(weatherData)
        } catch (e: IOException) {
            e.printStackTrace()
            // Obsługa błędu, jeśli nie można otworzyć pliku JSON
        }
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