package com.example.friendlist

data class CurrentWeather(
    val coord: CurrentWeatherResult,
    val weather: List<CurrentWeatherResult>,
    val base: String,
    val main: CurrentWeatherResult,
    val visibility: Double,
    val wind: CurrentWeatherResult,
    val clouds: CurrentWeatherResult,
    val dt: Double,
    val sys: CurrentWeatherResult,
    val timezone: Double,
    val id: Double,
    val name: String,
    val cod: Double
)

data class CurrentWeatherResult(
    val temp : Double,
    val feels_like : Double,
    val id: Double,
    val description: String,
    val main: String,
    val temp_min : Double,
    val temp_max : Double,
    val pressure : Double,
    val humidity : Double,
)