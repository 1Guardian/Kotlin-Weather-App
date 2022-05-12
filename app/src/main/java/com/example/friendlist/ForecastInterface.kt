package com.example.friendlist

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastInterface {
    @GET("data/2.5/forecast?")
    fun getForecast(@Query("appid") appid: String, @Query("lat") lat: String, @Query("lon") lon: String, @Query("units") units: String): Call<Forecast>
}