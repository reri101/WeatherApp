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

class FavouriteListActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityFavouriteListBinding
    private lateinit var searchView: SearchView

    // Replace with your actual city data or fetched data from OpenWeatherMap API
    private val cityNameList: MutableList<String> = mutableListOf()
    private val filteredCityNameList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouriteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
    }

    // Optional function to fetch city data from OpenWeatherMap API
}
