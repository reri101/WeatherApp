package com.example.weatherapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.R
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.databinding.FragmentBasicWeatherBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class BasicWeatherFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit  var binding: FragmentBasicWeatherBinding
    private lateinit  var bindingMain: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        binding = FragmentBasicWeatherBinding.inflate(inflater, container, false)
        setUpWeatherInfo()
        return binding.root

    }



    fun setUpWeatherInfo(){
        sharedPreferences = requireContext().getSharedPreferences(
            "WeatherAppPrefs",
            Context.MODE_PRIVATE
        )
        val units = sharedPreferences.getString("units", "metric") ?: "metric"
        fetachWeatherData("Warsaw", units)

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
        binding.timeB.text = currentTime
    }

    private fun fetachWeatherData(cityName:String, units:String) {
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("https://api.openweathermap.org/data/2.5/").build().create(
            ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName,"pl","bd04d1ce49301ed0175976c62138cd19",units)
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    val temperature = responseBody.main.temp.toString()
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp = responseBody.main.temp_max.toString()
                    val minTemp = responseBody.main.temp_min.toString()
                    val desc = responseBody.weather.first().description;
                    var coordinates1= responseBody.coord.lat.toString()
                    coordinates1 = String.format(Locale.getDefault(), "%.2f", coordinates1.toDouble())
                    var coordinates2= responseBody.coord.lon.toString()
                    coordinates2 = String.format(Locale.getDefault(), "%.2f", coordinates2.toDouble())

                    var test= binding.temperatureB.text
                    Log.d("BasicWeatherFragment", "Fetching weather data for coordinates $test")




                    handler.post {
                    binding.temperatureB.text = binding.temperatureB.text.toString().replace("00:00",temperature)
                    test= binding.temperatureB.text
                    Log.d("BasicWeatherFragmentXXX", "Fetching weather data for coordinates $test")
                    binding.temperatureB.invalidate()


                    binding.weatherTypeB.text = binding.weatherTypeB.text.toString().replace("Sunny",condition)
                    binding.maxMinTempB.text = binding.maxMinTempB.text.toString().replace("Max",maxTemp)
                    binding.maxMinTempB.text = binding.maxMinTempB.text.toString().replace("Min",minTemp)
                    binding.dayOfDateB.text=dayName(System.currentTimeMillis())
                    binding.dateB.text=date(System.currentTimeMillis())
                    binding.descriptionB.text=desc
                    binding.cityNameB.text= binding.cityNameB.text.toString().plus(cityName)
                    binding.coordinatorB.text = binding.coordinatorB.text.toString().replace("00",coordinates1)
                    binding.coordinatorB.text = binding.coordinatorB.text.toString().replace("11",coordinates2)

                    val fahrenheit = getString(R.string.fahrenheitDegree)
                    val celsjusz = getString(R.string.celsjuszDegree)
                    if(units=="imperial"){
                        binding.temperatureB.text = binding.temperatureB.text.toString().replace(celsjusz,fahrenheit)
                        binding.maxMinTempB.text = binding.maxMinTempB.text.toString().replace(celsjusz,fahrenheit)
                    }else{
                        binding.temperatureB.text = binding.temperatureB.text.toString().replace(fahrenheit,celsjusz)
                        binding.maxMinTempB.text = binding.maxMinTempB.text.toString().replace(fahrenheit,celsjusz)
                    }
                    }

                    changeImagsAccordingToWeatherCondition(condition)
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
//                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationViewB.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
//                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationViewB.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
//                binding.root.setBackgroundResource(R.color.transparent)
                binding.lottieAnimationViewB.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
//                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationViewB.setAnimation(R.raw.snow)
            }
            else ->{
//                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationViewB.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationViewB.playAnimation()
    }
}