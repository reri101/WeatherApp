package com.example.weatherapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun getWeatherData(
        @Query("q") city:String,
        @Query("lang") lang:String,
        @Query("appid") appid:String,
        @Query("units") units:String
    ) : Call<WeatherApp>

    @GET("forecast")
    fun getNextDaysWeatherData(
        @Query("q") city:String,
        @Query("appid") appid:String,
        @Query("units") units:String
    ) : Call<WeatherApp>

}