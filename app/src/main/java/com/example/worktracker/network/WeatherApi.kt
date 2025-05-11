package com.example.worktracker.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for OpenWeatherMap API.
 */
interface WeatherApi {
    /**
     * Get current weather data by geographic coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param apiKey OpenWeatherMap API key
     * @param units Units of measurement (metric, imperial, standard)
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
    
    /**
     * Get current weather data by city name
     * @param cityName City name
     * @param apiKey OpenWeatherMap API key
     * @param units Units of measurement (metric, imperial, standard)
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
