package com.example.weatherapp

import SettingsFragment
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.fragments.BasicWeatherFragment
import com.example.weatherapp.fragments.FavouriteListFragment
import com.example.weatherapp.fragments.WeatherFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale


class MainActivity : AppCompatActivity(), BasicWeatherFragment.WeatherConditionListener {
    private lateinit var sharedPreferences: SharedPreferences
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions

        sharedPreferences = getSharedPreferences("WeatherAppPrefs",Context.MODE_PRIVATE)

        if (!isOnline()) {
            Toast.makeText(this, "Brak internetu!\nDane pogodowe mogą być nieaktualne", Toast.LENGTH_LONG).show()
        }
        else{
            val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
            var cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"
            fetachWeatherData(cityName, units)
        }

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
    fun selectTabInTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val pogodaTab = tabLayout.getTabAt(1)
        pogodaTab?.select()
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    override fun onWeatherConditionChanged(conditions: String) {
        changeImagsAccordingToWeatherCondition(conditions)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.constLayout, fragment)
        fragmentTransaction.commit()
    }

    private fun fetachWeatherData(cityName:String, units:String) {
        if (!shouldRefreshWeather()) {
            return
        }else {
            val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.openweathermap.org/data/2.5/").build().create(
                ApiInterface::class.java
            )
            val response =
                retrofit.getWeatherData(cityName, "en", "bd04d1ce49301ed0175976c62138cd19", units)
            response.enqueue(object : Callback<WeatherApp> {
                override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                    val responseBody = response.body()

                    if (response.isSuccessful && responseBody != null) {
                        var temperatureC = responseBody.main.temp
                        var temperatureF = (temperatureC * 9 / 5 + 32)
                        var minTempC = responseBody.main.temp_min
                        var maxTempC = responseBody.main.temp_max
                        var minTempF = (minTempC * 9 / 5 + 32)
                        var maxTempF = (maxTempC * 9 / 5 + 32)
                        if(units == "imperial"){
                            temperatureF = responseBody.main.temp
                            temperatureC = (temperatureF - 32) * 5 / 9
                            minTempF = minTempC
                            maxTempF = maxTempC
                            minTempC = (minTempF - 32) * 5 / 9
                            maxTempC = (maxTempF - 32) * 5 / 9
                        }
                        temperatureC = String.format(Locale.getDefault(), "%.2f", temperatureC).toDouble()
                        temperatureF = String.format(Locale.getDefault(), "%.2f", temperatureF).toDouble()
                        minTempC = String.format(Locale.getDefault(), "%.2f", minTempC).toDouble()
                        maxTempC = String.format(Locale.getDefault(), "%.2f", maxTempC).toDouble()
                        minTempF = String.format(Locale.getDefault(), "%.2f", minTempF).toDouble()
                        maxTempF = String.format(Locale.getDefault(), "%.2f", maxTempF).toDouble()

                        val weatherData = WeatherData(
                            cityName = cityName,
                            temperatureC = temperatureC.toString(),
                            temperatureF = temperatureF.toString(),
                            humidity = responseBody.main.humidity.toString(),
                            windSpeed = responseBody.wind.speed.toString(),
                            windDeg = responseBody.wind.deg.toString(),
                            sunRise = responseBody.sys.sunrise.toLong(),
                            sunSet = responseBody.sys.sunset.toLong(),
                            pressure = responseBody.main.pressure.toString(),
                            condition = responseBody.weather.firstOrNull()?.main ?: "unknown",
                            maxTempC = maxTempC.toString(),
                            minTempC = minTempC.toString(),
                            maxTempF = maxTempF.toString(),
                            minTempF = minTempF.toString(),
                            desc = responseBody.weather.first().description,
                            coordinates1 = String.format(Locale.getDefault(), "%.2f", responseBody.coord.lat.toString().toDouble()),
                            coordinates2 = String.format(Locale.getDefault(), "%.2f", responseBody.coord.lon.toString().toDouble())
                        )


                        val gson = Gson()
                        val json = gson.toJson(weatherData)
                        val cityNameLower= cityName.lowercase()

                        try {
                            val fos = openFileOutput("weather_data_$cityNameLower.json", Context.MODE_PRIVATE)
                            fos.write(json.toByteArray())
                            fos.close()
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
        val refreshFrequency = sharedPreferences.getInt("refreshFrequency", 6)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastRefreshTime
        val elapsedTimeHours = elapsedTime / (1000 * 60 )

        return elapsedTimeHours >= refreshFrequency
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