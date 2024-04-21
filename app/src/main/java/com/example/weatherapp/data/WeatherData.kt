package com.example.weatherapp.data

import com.example.weatherapp.dataND.NextDaysWeatherApp

data class WeatherData(
    val cityName: String,
    val temperature: String,
    val humidity: String,
    val windSpeed: String,
    val windDeg: String,
    val sunRise: Long,
    val sunSet: Long,
    val pressure: String,
    val condition: String,
    val maxTemp: String,
    val minTemp: String,
    val desc: String,
    val coordinates1: String,
    val coordinates2: String,
   // val nextDays: NextDaysWeatherApp
)