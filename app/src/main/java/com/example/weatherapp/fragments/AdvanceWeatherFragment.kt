package com.example.weatherapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.R
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.databinding.FragmentAdvanceWeatherBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.weatherapp.data.WeatherData
import com.google.gson.Gson
import java.io.IOException


class AdvanceWeatherFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentAdvanceWeatherBinding
    private lateinit var cityName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdvanceWeatherBinding.inflate(inflater, container, false)
        cityName = arguments?.getString("cityName").toString()
        if(cityName == "")
            cityName="Warsaw"
        setUpWeatherInfo()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setUpWeatherInfo()
    }


    fun setUpWeatherInfo(){
        sharedPreferences = requireContext().getSharedPreferences(
            "WeatherAppPrefs",
            Context.MODE_PRIVATE
        )
        val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
        fetachWeatherData(cityName, units)

        val handler = Handler(Looper.getMainLooper())
        val runnableCode = object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnableCode)
    }


    private fun updateCurrentTime() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val currentTime = sdf.format(Date())
        binding.time.text = currentTime
    }

    private fun fetachWeatherData(cityName:String, units:String) {
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("https://api.openweathermap.org/data/2.5/").build().create(
            ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName,"pl","bd04d1ce49301ed0175976c62138cd19",units)
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    val cName = cityName
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
                        val fos = requireContext().openFileOutput("weather_data_$cityNameLower.json", Context.MODE_PRIVATE)
                        fos.write(json.toByteArray())
                        fos.close()
                        Log.d("MainActivity", "Data saved to file")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }



                    binding.temperature.text = binding.temperature.text.toString().replace("00:00",weatherData.temperature)
                    binding.weatherType.text = binding.weatherType.text.toString().replace("Sunny",weatherData.condition)
                    binding.humidity.text = binding.humidity.text.toString().replace("000",weatherData.humidity)
                    binding.windSpeed.text = binding.windSpeed.text.toString().replace("0.00",weatherData.windSpeed)
                    binding.sunrise.text = time(weatherData.sunRise)
                    binding.sunset.text = time(weatherData.sunSet)
                    binding.pressure.text = binding.pressure.text.toString().replace("0000",weatherData.pressure)
                    binding.weatherCondition.text = binding.weatherCondition.text.toString().replace("00:00",weatherData.windDeg)
                    binding.maxMinTemp.text = binding.maxMinTemp.text.toString().replace("Max",weatherData.maxTemp)
                    binding.maxMinTemp.text = binding.maxMinTemp.text.toString().replace("Min",weatherData.minTemp)
                    binding.dayOfDate.text=dayName(System.currentTimeMillis())
                    binding.date.text=date(System.currentTimeMillis())
                    binding.description.text=weatherData.desc
                    binding.cityName.text= " $cName"
                    binding.coordinator.text = binding.coordinator.text.toString().replace("00",weatherData.coordinates1)
                    binding.coordinator.text = binding.coordinator.text.toString().replace("11",weatherData.coordinates2)

                    val fahrenheit = getString(R.string.fahrenheitDegree)
                    val celsjusz = getString(R.string.celsjuszDegree)
                    if(units=="imperial"){
                        binding.temperature.text = binding.temperature.text.toString().replace(celsjusz,fahrenheit)
                        binding.maxMinTemp.text = binding.maxMinTemp.text.toString().replace(celsjusz,fahrenheit)
                    }else{
                        binding.temperature.text = binding.temperature.text.toString().replace(fahrenheit,celsjusz)
                        binding.maxMinTemp.text = binding.maxMinTemp.text.toString().replace(fahrenheit,celsjusz)
                    }

                    changeImagsAccordingToWeatherCondition(weatherData.condition)
                    //Log.d("TAG", "onResponse: $windSpeed")

                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
            }
        })
    }
    private fun dayName(timestamp: Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun date(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    private fun changeImagsAccordingToWeatherCondition(conditions: String){
        when(conditions){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.lottieAnimationView.setAnimation(R.raw.sun2)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.lottieAnimationView.setAnimation(R.raw.sun2)
            }

        }
        binding.lottieAnimationView.playAnimation()
    }
    fun newInstance(cityName: String?): AdvanceWeatherFragment {
        val fragment = AdvanceWeatherFragment()
        val args = Bundle().apply {
            putString("cityName", cityName)
        }
        fragment.arguments = args
        return fragment
    }
}