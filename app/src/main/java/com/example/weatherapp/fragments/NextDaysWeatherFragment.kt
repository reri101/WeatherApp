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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.dataND.NextDayWeatherItem
import com.example.weatherapp.adapter.NextDaysWeatherAdapter
import com.example.weatherapp.dataND.NextDayWeatherList
import com.example.weatherapp.databinding.FragmentNextDaysWeatherBinding
import com.example.weatherapp.databinding.NextDaysWeatherFragmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class NextDaysWeatherFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentNextDaysWeatherBinding
    private lateinit var cityName: String
    private val handler = Handler(Looper.getMainLooper())
    private var isWeatherUpdateThreadRunning = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNextDaysWeatherBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)

        return binding.root
    }

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
                val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
                cityName = sharedPreferences.getString("city_setted", "Warsaw") ?: "Warsaw"
//                Log.d("watek", ":--- $cityName")
                setupRecyclerView(cityName,units)
                handler.postDelayed(this, 10000)
            }
        }
        handler.post(runnable)
    }

    private fun setupRecyclerView(cityName: String, units: String) {

        val shouldFetchFromNetwork = shouldFetchWeatherFromNetwork()
//        Log.d("citynamee", ":x $cityName")
//        Log.d("shouldFetchFromNetworkND", ": $shouldFetchFromNetwork")
        if (shouldFetchFromNetwork) {
//            Log.d("watek", ":.. $cityName")
            val apiRequestUrl = generateWeatherApiRequestUrl(cityName, units)
            fetchWeatherData(apiRequestUrl)
        }else{
//            Log.d("watek", ":.c. $cityName")
            val nextDayWeatherList = loadWeatherDataFromFile(cityName)
            activity?.runOnUiThread {
                val adapter = NextDaysWeatherAdapter(requireContext(),nextDayWeatherList.weatherList)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }


    }

    private fun generateWeatherApiRequestUrl(cityName: String, units: String): String {
        val apiKey = "bd04d1ce49301ed0175976c62138cd19"
        return "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey&units=$units"
    }

    private fun fetchWeatherData(apiRequestUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(apiRequestUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    val apiResponse = responseBody.string()
                    val nextDayWeatherList = generateNextDayWeatherList(apiResponse)
                    saveWeatherDataToFile(cityName,nextDayWeatherList)
                    activity?.runOnUiThread {
                        val adapter = NextDaysWeatherAdapter(requireContext(),nextDayWeatherList.weatherList)
                        binding.recyclerView.adapter = adapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }

                    sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putLong("lastRefreshTime_nd"+cityName, System.currentTimeMillis())
                    editor.apply()
                }
            }
        })
    }
    private fun shouldFetchWeatherFromNetwork(): Boolean {
        val lastRefreshTime = sharedPreferences.getLong("lastRefreshTime_nd" + cityName, -1L)
        if (lastRefreshTime == -1L) {
            return true
        }
        val refreshFrequency = sharedPreferences.getInt("refreshFrequency", 6)
        val currentTime = System.currentTimeMillis()
        val elapsedTimeSinceLastRefresh = currentTime - lastRefreshTime
        val elapsedTimeInHours = elapsedTimeSinceLastRefresh / (1000 * 60)
        return elapsedTimeInHours >= refreshFrequency
    }

    fun generateNextDayWeatherList(apiResponse: String): NextDayWeatherList {
        val nextDayWeatherList = mutableListOf<NextDayWeatherItem>()

        try {
            val jsonObject = JSONObject(apiResponse)
            val jsonArray = jsonObject.getJSONArray("list")

            for (i in 0 until jsonArray.length()) {
                val weatherObj = jsonArray.getJSONObject(i)
                val dtTxt = weatherObj.getString("dt_txt")

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(dtTxt)
                val calendar = Calendar.getInstance()
                calendar.time = date

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if (hour == 15) {
                    val mainObj = weatherObj.getJSONObject("main")
                    val weatherArray = weatherObj.getJSONArray("weather")
                    val weather = weatherArray.getJSONObject(0)
                    val windObj = weatherObj.getJSONObject("wind")

                    var temperatureC = mainObj.getDouble("temp")
                    var temperatureF = (temperatureC * 9 / 5 + 32)
                    val units = sharedPreferences.getString("temperatureUnit", "metric") ?: "metric"
                    if(units == "imperial"){
                        temperatureF = mainObj.getDouble("temp")
                        temperatureC = (temperatureF - 32) * 5 / 9
                    }
                    val weatherDescription = weather.getString("description")
                    val weatherCondition = weather.getString("main")
                    val windSpeed = windObj.getDouble("speed")
                    val humidity = mainObj.getDouble("humidity")

                    val dayDate = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(date)

                    val fahrenheit = getString(R.string.fahrenheitDegree)
                    val celsjusz = getString(R.string.celsjuszDegree)
                    temperatureC = String.format("%.2f", temperatureC).toDouble()
                    temperatureF = String.format("%.2f", temperatureF).toDouble()

                    val nextDayWeatherItem = NextDayWeatherItem(
                        dayDate = dayDate,
                        condition = weatherCondition,
                        temperatureC = "$temperatureC $celsjusz",
                        temperatureF = "$temperatureF $fahrenheit",
                        weatherDescription = weatherDescription,
                        windSpeed = "$windSpeed m/s",
                        humidity = "$humidity %"
                    )

                    nextDayWeatherList.add(nextDayWeatherItem)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val nextDWL = NextDayWeatherList(cityName,nextDayWeatherList)
        return nextDWL
    }
    private fun saveWeatherDataToFile(cityName: String, weatherDataList: NextDayWeatherList) {
        val gson = Gson()
        val json = gson.toJson(weatherDataList)
        val fileName = cityName+"_nextd.json"
        try {
            val fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(json.toByteArray())
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun loadWeatherDataFromFile(cityName: String) : NextDayWeatherList{
        val fileName = cityName+"_nextd.json"
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
            val weatherDataList = gson.fromJson(sb.toString(), NextDayWeatherList::class.java)
            return weatherDataList

        } catch (e: IOException) {
            e.printStackTrace()
            return NextDayWeatherList(cityName, emptyList())
        }
    }


    fun newInstance(cityName: String?): NextDaysWeatherFragment {
            val fragment = NextDaysWeatherFragment()
            val args = Bundle().apply {
                putString("cityName", cityName)
            }
            fragment.arguments = args
            return fragment
        }

}