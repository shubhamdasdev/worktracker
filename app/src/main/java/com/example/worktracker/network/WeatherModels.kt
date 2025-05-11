package com.example.worktracker.network

/**
 * Data models for the OpenWeatherMap API responses.
 */

data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int
)

data class Coord(
    val lon: Double,
    val lat: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)

data class Clouds(
    val all: Int
)

data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

/**
 * Simplified weather data model for UI display and storage
 */
data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val icon: String,
    val timestamp: Long,
    val location: String
) {
    companion object {
        fun fromResponse(response: WeatherResponse): WeatherData {
            return WeatherData(
                temperature = response.main.temp,
                feelsLike = response.main.feels_like,
                description = if (response.weather.isNotEmpty()) response.weather[0].description else "",
                icon = if (response.weather.isNotEmpty()) response.weather[0].icon else "",
                timestamp = response.dt,
                location = response.name
            )
        }
    }
}
