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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class AdvanceWeatherFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentAdvanceWeatherBinding
    private lateinit var cityName: String
    private val handler = Handler(Looper.getMainLooper())
    private var isWeatherUpdateThreadRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdvanceWeatherBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"
        //setUpWeatherInfo()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
        cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"

        if (!isWeatherUpdateThreadRunning) {
            startWeatherUpdateThread()
            isWeatherUpdateThreadRunning = true
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        isWeatherUpdateThreadRunning = false
    }

    private fun startWeatherUpdateThread() {
        val runnable = object : Runnable {
            override fun run() {
                //Log.d("watek", ":--- $cityName")
                setUpWeatherInfo()
                handler.postDelayed(this, 10000)
            }
        }
        handler.post(runnable)
    }


    fun setUpWeatherInfo(){
        val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
        cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"

        //Log.d("watek", ":cc $cityName")
        loadWeatherDataFromFile(cityName, units)

        updateCurrentTime()
    }


    private fun updateCurrentTime() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val currentTime = sdf.format(Date())
        binding.time?.text = currentTime
    }
    private fun loadWeatherDataFromFile(cityName: String, units: String) {
        val fileName = cityName+"_bas.json"
        try {
            val fis = requireContext().openFileInput(fileName)
            val isr = InputStreamReader(fis)
            val bufferedReader = BufferedReader(isr)
            val sb = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            fis.close()
            isr.close()
            val gson = Gson()
            val weatherData = gson.fromJson(sb.toString(), WeatherData::class.java)
            updateUI(weatherData, units)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun updateUI(weatherData: WeatherData,units: String) {
        binding.temperature?.text = binding.temperature?.text.toString().replace("00:00",weatherData.temperatureC)
        binding.weatherType?.text = binding.weatherType?.text.toString().replace("Sunny",weatherData.condition)
        binding.humidity.text = binding.humidity.text.toString().replace("000",weatherData.humidity)
        binding.windSpeed.text = binding.windSpeed.text.toString().replace("0.00",weatherData.windSpeed)
        binding.sunrise.text = time(weatherData.sunRise)
        binding.sunset.text = time(weatherData.sunSet)
        binding.pressure.text = binding.pressure.text.toString().replace("0000",weatherData.pressure)
        binding.weatherCondition.text = binding.weatherCondition.text.toString().replace("00:00",weatherData.windDeg)
        binding.dayOfDate?.text=dayName(System.currentTimeMillis())
        binding.date?.text=date(System.currentTimeMillis())
        binding.description?.text=weatherData.desc
        binding.cityName?.text= " $cityName"
        binding.coordinator?.text = binding.coordinator?.text.toString().replace("00",weatherData.coordinates1)
        binding.coordinator?.text = binding.coordinator?.text.toString().replace("11",weatherData.coordinates2)

        val fahrenheit = getString(R.string.fahrenheitDegree)
        val celsjusz = getString(R.string.celsjuszDegree)
        if(units=="imperial"){
            binding.temperature?.text = weatherData.temperatureF + " $fahrenheit"
            binding.maxMinTemp?.text = weatherData.minTempF +" $fahrenheit - " + weatherData.maxTempF + " $fahrenheit"
        }else{
            binding.temperature?.text = weatherData.temperatureC + " $celsjusz"
            binding.maxMinTemp?.text = weatherData.minTempC +" $celsjusz - " + weatherData.maxTempC + " $celsjusz"
        }

        changeImagsAccordingToWeatherCondition(weatherData.condition)
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
                binding.lottieAnimationView?.setAnimation(R.raw.sun2)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
                binding.lottieAnimationView?.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
                binding.lottieAnimationView?.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.lottieAnimationView?.setAnimation(R.raw.snow)
            }
            else ->{
                binding.lottieAnimationView?.setAnimation(R.raw.sun2)
            }

        }
        binding.lottieAnimationView?.playAnimation()
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