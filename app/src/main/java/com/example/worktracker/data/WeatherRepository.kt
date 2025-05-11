package com.example.worktracker.data

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.example.worktracker.Constants
import com.example.worktracker.network.WeatherApiClient
import com.example.worktracker.network.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for weather data operations.
 */
interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherData>
    suspend fun getCurrentWeatherByCity(cityName: String): Result<WeatherData>
    suspend fun getLastKnownLocation(context: Context): Location?
}

/**
 * Implementation of WeatherRepository.
 */
class WeatherRepositoryImpl(
    private val context: Context,
    private val sharedPref: SharedPreferencesRepository
) : WeatherRepository {
    
    private val TAG = "WeatherRepository"
    
    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = WeatherApiClient.getApiKey(context)
                val response = WeatherApiClient.api.getCurrentWeatherByCoordinates(
                    lat = lat,
                    lon = lon,
                    apiKey = apiKey
                )
                
                // Save the last known location
                sharedPref.putString(Constants.WEATHER_LAST_LAT_KEY, lat.toString())
                sharedPref.putString(Constants.WEATHER_LAST_LON_KEY, lon.toString())
                
                Result.success(WeatherData.fromResponse(response))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather data: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getCurrentWeatherByCity(cityName: String): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = WeatherApiClient.getApiKey(context)
                val response = WeatherApiClient.api.getCurrentWeatherByCity(
                    cityName = cityName,
                    apiKey = apiKey
                )
                Result.success(WeatherData.fromResponse(response))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather data by city: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getLastKnownLocation(context: Context): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Try to get the last known location from GPS or network
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
            
            // If we couldn't get a location, try to use the last saved coordinates
            if (bestLocation == null) {
                val lastLat = sharedPref.getString(Constants.WEATHER_LAST_LAT_KEY, "")?.toDoubleOrNull()
                val lastLon = sharedPref.getString(Constants.WEATHER_LAST_LON_KEY, "")?.toDoubleOrNull()
                
                if (lastLat != null && lastLon != null) {
                    Location("cache").apply {
                        latitude = lastLat
                        longitude = lastLon
                        bestLocation = this
                    }
                }
            }
            
            bestLocation
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location: ${e.message}")
            null
        }
    }
}
