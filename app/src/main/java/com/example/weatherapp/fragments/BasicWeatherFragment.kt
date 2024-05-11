package com.example.weatherapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weatherapp.R
import com.example.weatherapp.adapter.FragmentPagedAdapter
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.databinding.FragmentBasicWeatherBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


// dodanie watkow aby odswierzac informacje pogoodwe i widok gdy minie okreslony czas zamiast gdy ponownie wejdziemy
// co 2 min odswierzanie
// po wyszukaniu miasta w ulubione powinno zamieniac widok ale nie dodawac do ulubionych
// dodawanie do ulubionych powinno byc dodatkowe np w wyszukiwaniu w prawym rogu byl by guzik dodac do ulubionych
// naprawic widok po zmianie na farenheita

class BasicWeatherFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentBasicWeatherBinding
    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var cityName: String
    private val handler = Handler(Looper.getMainLooper())
    private var isWeatherUpdateThreadRunning = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        binding = FragmentBasicWeatherBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"
        //Log.d("citynamee", "cnn $cityName")

        //startWeatherUpdateThread()
        return binding.root
    }

    //ondestrv




    override fun onResume() {
        super.onResume()
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

    // onpause

    private fun setUpWeatherInfo() {
        val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
        cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"

        val shouldFetchFromNetwork = shouldFetchWeatherFromNetwork()
        //Log.d("shouldFetchFromNetwork", ": $shouldFetchFromNetwork")
        //Log.d("watek", ":xxx $cityName")
        if (shouldFetchFromNetwork) {
            //Log.d("watek", ":x $cityName")
            fetachWeatherData(cityName, units)
        }else{
            loadWeatherDataFromFile(cityName,units)
        }

// nowy watek

        updateCurrentTime()
    }

    private fun updateCurrentTime() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val currentTime = sdf.format(Date())
        binding.timeB.text = currentTime
    }

    private fun fetachWeatherData(cityName: String, units: String) {
        //Log.d("citynamee", ": $cityName")
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName, "en", "bd04d1ce49301ed0175976c62138cd19", units)
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                var responseBody = response.body()

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

                    saveWeatherDataToFile(cityName, weatherData)

                    val editor = sharedPreferences.edit()
                    editor.putLong("lastRefreshTime_x"+cityName, System.currentTimeMillis())
                    //Log.d("shouldFetchFromNetwork", ":1 lastRefreshTime_x"+cityName)
                    editor.apply()

                    updateUI(weatherData,units)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
            }
        })
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
    private fun saveWeatherDataToFile(cityName: String, weatherData: WeatherData) {
        val gson = Gson()
        val json = gson.toJson(weatherData)
        val fileName = cityName+"_bas.json"
        try {
            val fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(json.toByteArray())
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun updateUI(weatherData: WeatherData,units: String) {
        binding.temperatureB.text = binding.temperatureB.text.toString().replace("00:00", weatherData.temperatureC)
        binding.weatherTypeB?.text = binding.weatherTypeB?.text.toString().replace("Sunny", weatherData.condition)
        binding.dayOfDateB.text = dayName(System.currentTimeMillis())
        binding.dateB.text = date(System.currentTimeMillis())
        binding.descriptionB.text = weatherData.desc
        binding.cityNameB.text = " $cityName"
        val coordiantes1 = weatherData.coordinates1
        val coordiantes2 = weatherData.coordinates2
        binding.coordinatorB.text = "N $coordiantes1°, $coordiantes2°"
        binding.coordinatorB.text = binding.coordinatorB.text.toString().replace("11", weatherData.coordinates2)

        val fahrenheit = getString(R.string.fahrenheitDegree)
        val celsjusz = getString(R.string.celsjuszDegree)
        if (units == "imperial") {
            binding.temperatureB.text = weatherData.temperatureF + " $fahrenheit"
            binding.maxMinTempB.text = weatherData.minTempF +" $fahrenheit - " + weatherData.maxTempF + " $fahrenheit"
        } else {
            binding.temperatureB.text = weatherData.temperatureC + " $celsjusz"
            binding.maxMinTempB.text = weatherData.minTempC +" $celsjusz - " + weatherData.maxTempC + " $celsjusz"
        }

        (activity as? WeatherConditionListener)?.onWeatherConditionChanged(weatherData.condition)
        changeImagsAccordingToWeatherCondition(weatherData.condition)
    }

    private fun changeImagsAccordingToWeatherCondition(conditions: String){
        when(conditions){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.lottieAnimationViewB.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
                binding.lottieAnimationViewB.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
                binding.lottieAnimationViewB.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.lottieAnimationViewB.setAnimation(R.raw.snow)
            }
            else ->{
                binding.lottieAnimationViewB.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationViewB.playAnimation()
    }

    private fun shouldFetchWeatherFromNetwork(): Boolean {
        val lastRefreshTime = sharedPreferences.getLong("lastRefreshTime_x"+cityName, 0L)
        //Log.d("shouldFetchFromNetwork", ": lastRefreshTime_x"+cityName)
        val refreshFrequency = sharedPreferences.getInt("refreshFrequency", 6)
        val currentTime = System.currentTimeMillis()
        val elapsedTimeSinceLastRefresh = currentTime - lastRefreshTime
        val elapsedTimeInHours = elapsedTimeSinceLastRefresh / (1000 * 60)
        return elapsedTimeInHours >= refreshFrequency
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun date(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    interface WeatherConditionListener {
        fun onWeatherConditionChanged(conditions: String)
    }


}
