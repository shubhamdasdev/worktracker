package com.example.worktracker.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for WorldTimeAPI (https://worldtimeapi.org/api/).
 */
interface WorldTimeApi {
    @GET("api/timezone")
    suspend fun listZones(): List<String>

    @GET("api/timezone/{zoneId}")
    suspend fun zoneInfo(@Path("zoneId") zoneId: String): WorldTimeResponse
}

/**
 * Data model for the response from WorldTimeAPI.
 */
data class WorldTimeResponse(
    val timezone: String,
    val utc_offset: String,
    val datetime: String
)
