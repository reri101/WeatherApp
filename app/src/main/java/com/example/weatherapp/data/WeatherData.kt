package com.example.weatherapp.data


data class WeatherData(
    val cityName: String,
    val temperatureC: String,
    val temperatureF: String,
    val humidity: String,
    val windSpeed: String,
    val windDeg: String,
    val sunRise: Long,
    val sunSet: Long,
    val pressure: String,
    val condition: String,
    val maxTempC: String,
    val minTempC: String,
    val maxTempF: String,
    val minTempF: String,
    val desc: String,
    val coordinates1: String,
    val coordinates2: String,
)