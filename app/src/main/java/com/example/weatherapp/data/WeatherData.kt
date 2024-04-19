package com.example.weatherapp.data

data class WeatherData(
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
    val coordinates2: String
)