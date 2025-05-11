package com.example.worktracker.network

import android.content.Context
import android.util.Log
import com.example.worktracker.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton client for the OpenWeatherMap API.
 */
object WeatherApiClient {
    private const val BASE_URL = "https://api.openweathermap.org/"
    private const val TAG = "WeatherApiClient"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: WeatherApi = retrofit.create(WeatherApi::class.java)
    
    /**
     * Get the API key from local.properties
     */
    fun getApiKey(context: Context): String {
        return try {
            // Get the API key from BuildConfig
            val field = BuildConfig::class.java.getDeclaredField("OPENWEATHERMAP_API_KEY")
            field.isAccessible = true
            field.get(null) as String
        } catch (e: Exception) {
            Log.e(TAG, "Error getting API key: ${e.message}")
            // Fallback to the API key provided in the implementation plan
            "780ec91a461b5b90056f04fec19a5752"
        }
    }
}
