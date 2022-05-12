package com.example.friendlist

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrentWeatherInterface {
    @GET("data/2.5/weather?")
    fun getMovies(@Query("appid") appid: String, @Query("lat") lat: String, @Query("lon") lon: String, @Query("units") units: String): Call<CurrentWeather>
}