package com.example.weatherapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityFavouriteListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// dodac mozliwosc usuniecia
// mozna by sprobowac dodac json
// ma byc jedna aktywnosc

// fragmenty lub viewpager w wersji 2
// 3 erkany , erkan podstawowy ekran ze szczegolami i ekran na przyszle dni
// gorne menu do zmiany miasta, ekranu
// po za zmianą temperatury tez zmiana czasu co ile sie pobiera dane
// na tablecie wszystkie fragmenty na jednym widoku a na telefonie na roznych
//wiev holder wiev pager   adapter
//fragment manager albo viewpager v2
// dla tabletu mozna wstawic componentFragment zeby na stale wstawic 3 fragmenty
class FavouriteListActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityFavouriteListBinding
    private lateinit var searchView: SearchView

    private val cityNameList: MutableList<String> = mutableListOf()
    private val filteredCityNameList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_list)

        sharedPreferences = getSharedPreferences("WeatherAppPreferences", MODE_PRIVATE)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    }



}
