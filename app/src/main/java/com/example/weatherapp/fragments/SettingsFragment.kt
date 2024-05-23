import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.adapter.NextDaysWeatherAdapter
import com.example.weatherapp.data.ApiInterface
import com.example.weatherapp.data.WeatherApp
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.dataND.NextDayWeatherItem
import com.example.weatherapp.dataND.NextDayWeatherList
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)

        val temperatureUnitRadioGroup: RadioGroup = view.findViewById(R.id.temperatureUnitRadioGroup)
        val refreshFrequencyEditText: EditText = view.findViewById(R.id.refreshFrequencyEditText)

        val currentTemperatureUnit = sharedPreferences.getString("temperatureUnit", "metric")
        when (currentTemperatureUnit) {
            "metric" -> temperatureUnitRadioGroup.check(R.id.celsiusRadioButton)
            "imperial" -> temperatureUnitRadioGroup.check(R.id.fahrenheitRadioButton)
        }

        val currentRefreshFrequency = sharedPreferences.getInt("refreshFrequency", 6)
        refreshFrequencyEditText.setText(currentRefreshFrequency.toString())

        temperatureUnitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedUnit = if (checkedId == R.id.celsiusRadioButton) "metric" else "imperial"
            sharedPreferences.edit().putString("temperatureUnit", selectedUnit).apply()
        }

        refreshFrequencyEditText.addTextChangedListener {
            val frequencyText = it.toString()
            val frequency = frequencyText.toIntOrNull()
            if (frequency != null && frequency >= 0) {
                sharedPreferences.edit().putInt("refreshFrequency", frequency).apply()
            } else {
                Toast.makeText(requireContext(), "Podaj poprawną liczbę minut", Toast.LENGTH_SHORT).show()
            }
        }

        val refreshWeatherButton: Button = view.findViewById(R.id.refreshWeatherButton)
        refreshWeatherButton.setOnClickListener {

                refreshWeatherDataForFavoriteCities()
        }

        return view
    }



    private fun refreshWeatherDataForFavoriteCities() {
        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
            return
        }
        val sharedPreferences2 = requireContext().getSharedPreferences("FavCities4", Context.MODE_PRIVATE)
        val favoriteCitiesSet = sharedPreferences2.getStringSet("favoriteCities", emptySet())?.toMutableSet() ?: mutableSetOf()
        val temperatureUnitRadioGroup: RadioGroup = requireActivity().findViewById(R.id.temperatureUnitRadioGroup)
        val units = if (temperatureUnitRadioGroup.checkedRadioButtonId == R.id.celsiusRadioButton) "metric" else "imperial"

        favoriteCitiesSet.forEach { cityName ->
            fetchWeatherData(cityName, units)
            val apiRequestUrl = generateWeatherApiRequestUrlND(cityName, units)
            fetchWeatherDataND(cityName,apiRequestUrl)
            Toast.makeText(requireContext(), "Odświeżanie informacji o pogodzie dla $cityName", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    private fun generateWeatherApiRequestUrlND(cityName: String, units: String): String {
        val apiKey = "bd04d1ce49301ed0175976c62138cd19"
        return "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey&units=$units"
    }

    private fun fetchWeatherDataND(cityName: String,apiRequestUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(apiRequestUrl)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null){
                    val apiResponse = responseBody.string()
                    val nextDayWeatherList = generateNextDayWeatherListND(cityName,apiResponse)
                    saveWeatherDataToFileND(cityName,nextDayWeatherList)

                    sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putLong("lastRefreshTime_nd"+cityName, System.currentTimeMillis())
                    editor.apply()
                }
            }
        })
    }
    fun generateNextDayWeatherListND(cityName: String, apiResponse: String): NextDayWeatherList {
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
    private fun saveWeatherDataToFileND(cityName: String, weatherDataList: NextDayWeatherList) {
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
    private fun fetchWeatherData(cityName: String, units: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName, "pl", "bd04d1ce49301ed0175976c62138cd19", units)
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
                    saveWeatherDataToFile(cityName, weatherData)

                    val editor = sharedPreferences.edit()
                    editor.putLong("lastRefreshTime_x"+cityName, System.currentTimeMillis())
                    editor.apply()

                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
            }
        })
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
}
