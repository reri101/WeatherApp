package com.example.weatherapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val binding: ActivityMenuBinding by lazy {
        ActivityMenuBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        binding.switchCentigrade.setOnCheckedChangeListener{ _, isChecked ->
            val units = if (isChecked) "imperial" else "metric"
            sharedPreferences.edit().putString("units", units).apply()
        }


        val btnAppTitle = findViewById<TextView>(R.id.appTitle)
        btnAppTitle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnFavList = findViewById<Button>(R.id.localizationList)
        btnFavList.setOnClickListener {
            val intent = Intent(this, FavouriteListActivity::class.java)
            startActivity(intent)
        }
    }
}