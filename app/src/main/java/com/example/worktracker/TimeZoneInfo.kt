package com.example.worktracker

import android.util.Log
import com.example.worktracker.network.NetworkModule
import com.example.worktracker.network.WorldTimeApi
import com.example.worktracker.network.WorldTimeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.zone.ZoneRulesException
import kotlin.math.abs

object TimeZoneInfo {
    private const val TAG = "TimeZoneInfo"
    private val api: WorldTimeApi = NetworkModule.api
    var timeZoneList: List<Triple<String, String, String>> = emptyList()
        private set
    val letterToIndexMap = mutableMapOf<String, Int>()

    fun getTimeZoneDisplay(timeZoneId: String): String {
        return timeZoneList.find { it.first == timeZoneId }?.second ?: timeZoneId
    }

    // Fallback data in case API fails
    private val timeZoneData = listOf(
        Pair("America/New_York","New York / USA"),
        Pair("America/Los_Angeles","Los Angeles / USA"),
        Pair("America/Chicago","Chicago / USA"),
        Pair("Europe/London","London / UK"),
        Pair("Europe/Paris","Paris / France"),
        Pair("Asia/Tokyo","Tokyo / Japan"),
        Pair("Australia/Sydney","Sydney / Australia"),
        Pair("Etc/UTC","UTC (Coordinated Universal Time)")
    )

    // Initialize with fallback data
    init {
        val tempList = mutableListOf<Triple<String, String, String>>()
        val instant = Instant.now()
        timeZoneData.forEach {
            try {
                val zoneId = ZoneId.of(it.first)
                val offset = zoneId.rules.getOffset(instant).formatOffset()
                tempList.add(Triple(it.first, it.second, offset))
            } catch (e: Exception) {
                Log.e(TAG, "Error with fallback zone ${it.first}", e)
            }
        }
        timeZoneList = tempList
        
        timeZoneList.forEachIndexed { index, timeZone ->
            val startingLetter = timeZone.second.firstOrNull()?.toString() ?: ""
            if (startingLetter !in letterToIndexMap) {
                letterToIndexMap[startingLetter] = index
            }
        }
    }

    /**
     * Fetches time zones and their offsets from WorldTimeAPI, populates list and index map.
     */
    suspend fun fetchTimeZones() {
        withContext(Dispatchers.IO) {
            try {
                // First try to get the list of all zones
                val zones = api.listZones()
                
                // Then fetch details for each zone
                val newTimeZones = fetchZoneDetails(zones)
                
                // Update the data if we got anything
                updateTimeZoneData(newTimeZones)
            } catch (e: Exception) {
                Log.e(TAG, "Error listing zones", e)
                // Keep using fallback data
            }
        }
    }
    
    private suspend fun fetchZoneDetails(zones: List<String>): List<Triple<String, String, String>> {
        val result = mutableListOf<Triple<String, String, String>>()
        zones.forEach { id ->
            try {
                val resp: WorldTimeResponse = api.zoneInfo(id)
                result.add(Triple(id, resp.timezone, resp.utc_offset))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching zone $id", e)
            }
        }
        return result
    }
    
    private fun updateTimeZoneData(newZones: List<Triple<String, String, String>>) {
        // Only update if we got data
        if (newZones.isEmpty()) return
        
        // Update the list
        timeZoneList = newZones
        
        // Rebuild the index map
        letterToIndexMap.clear()
        for ((index, tz) in timeZoneList.withIndex()) {
            val letter = tz.second.firstOrNull()?.toString() ?: ""
            if (!letterToIndexMap.containsKey(letter)) {
                letterToIndexMap[letter] = index
            }
        }
    }
    
    private fun ZoneOffset.formatOffset(): String {
        val totalMinutes = this.totalSeconds / 60
        val hours = totalMinutes / 60
        val minutes = abs(totalMinutes % 60)

        return when {
            hours == 0 && minutes == 0 -> "GMT+0"
            minutes == 0 -> String.format("GMT%+d", hours)
            else -> String.format("GMT%+d:%02d", hours, minutes)
        }
    }
}
