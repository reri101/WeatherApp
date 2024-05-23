package com.example.weatherapp.dataND

data class NextDayWeatherItem(
    val dayDate: String,
    val condition: String,
    val temperatureC: String,
    val temperatureF: String,
    val weatherDescription: String,
    val windSpeed: String,
    val humidity: String
)