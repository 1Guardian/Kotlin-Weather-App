package com.example.friendlist

data class Forecast(
    val cod: String,
    val list: List<ForecastResult>,
)

data class ForecastResult(
    val main: ForecastMain,
    val weather: List<ForecastWeather>,
    val visibility: Double
)

data class ForecastMain(
    val temp : Double,
    val humidity: Double
)

data class ForecastWeather(
    val id : Double,
    val main: String
)