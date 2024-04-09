package com.example.weatherapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        val units = sharedPreferences.getString("units", "metric") ?: "metric"

        fetachWeatherData("Warsaw", units)

    }

    private fun fetachWeatherData(cityName:String, units:String) {
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("https://api.openweathermap.org/data/2.5/").build().create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName,"bd04d1ce49301ed0175976c62138cd19",units)
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity.toString()
                    val windSpeed = responseBody.wind.speed.toString()
                    val sunRise = responseBody.sys.sunrise.toString()
                    val sunSet = responseBody.sys.sunset.toString()
                    val pressure = responseBody.main.pressure.toString()
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp = responseBody.main.temp_max.toString()
                    val minTemp = responseBody.main.temp_min.toString()


                    binding.temperature.text = binding.temperature.text.toString().replace("00:00",temperature)
                    binding.weatherType.text = binding.weatherType.text.toString().replace("Sunny",condition)
                    binding.humidity.text = binding.humidity.text.toString().replace("000",humidity)
                    binding.windSpeed.text = binding.windSpeed.text.toString().replace("0.00",windSpeed)
                    binding.sunrise.text = binding.sunrise.text.toString().replace("00:00",sunRise)
                    binding.sunset.text = binding.sunset.text.toString().replace("00:00",sunSet)
                    binding.pressure.text = binding.pressure.text.toString().replace("0000",pressure)
                    binding.weatherCondition.text = binding.weatherCondition.text.toString().replace("Sunny",condition)
                    binding.maxTemp.text = binding.maxTemp.text.toString().replace("00:00",maxTemp)
                    binding.minTemp.text = binding.minTemp.text.toString().replace("00:00",minTemp)
                    binding.dayOfDate.text=dayName(System.currentTimeMillis())
                    binding.date.text=date(System.currentTimeMillis())
                    binding.cityName.text= binding.cityName.text.toString().plus(cityName)
                    //Log.d("TAG", "onResponse: $windSpeed")

                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
            }
        })
    }
    fun dayName(timestamp: Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    fun date(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
}