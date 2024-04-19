package com.example.weatherapp.dataND

data class NextDaysWeatherApp(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<dayliWeather>,
    val message: Int
)