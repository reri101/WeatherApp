package com.example.weatherapp.dataND

data class NextDayWeatherItem(
    val dayDate: String,
    val condition: String,
    val temperature: String,
    val weatherDescription: String,
    val windSpeed: String,
    val humidity: String
    // Dodaj inne pola, jeśli są potrzebne
)