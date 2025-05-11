package com.example.worktracker

import android.util.Log
import com.example.worktracker.network.NetworkModule
import com.example.worktracker.network.WorldTimeApi
import com.example.worktracker.network.WorldTimeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        Pair("Europe/Berlin","Berlin / Germany"),
        Pair("Europe/Rome","Rome / Italy"),
        Pair("Europe/Madrid","Madrid / Spain"),
        Pair("Asia/Tokyo","Tokyo / Japan"),
        Pair("Asia/Shanghai","Shanghai / China"),
        Pair("Asia/Singapore","Singapore"),
        Pair("Asia/Dubai","Dubai / UAE"),
        Pair("Australia/Sydney","Sydney / Australia"),
        Pair("Australia/Melbourne","Melbourne / Australia"),
        Pair("Africa/Johannesburg","Johannesburg / South Africa"),
        Pair("Africa/Cairo","Cairo / Egypt"),
        Pair("America/Sao_Paulo","Sao Paulo / Brazil"),
        Pair("America/Mexico_City","Mexico City / Mexico"),
        Pair("America/Toronto","Toronto / Canada"),
        Pair("Pacific/Auckland","Auckland / New Zealand"),
        Pair("Etc/UTC","UTC (Coordinated Universal Time)")
    )

    // Initialize with fallback data and ensure alphabet map is populated
    init {
        // Initialize the letter map with the full alphabet
        // This ensures the alphabet sidebar is always displayed
        ('A'..'Z').forEachIndexed { index, char -> 
            letterToIndexMap[char.toString()] = index
        }
        
        // Then initialize the fallback data
        initializeFallbackData()
    }
    
    private fun initializeFallbackData() {
        // Only initialize if the list is empty to avoid overwriting existing data
        if (timeZoneList.isEmpty()) {
            Log.d(TAG, "Initializing fallback timezone data")
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
            timeZoneList = tempList.sortedBy { it.second }
            
            // Build the letter index map
            updateLetterIndexMap()
            
            Log.d(TAG, "Fallback data initialized with ${timeZoneList.size} timezones")
        } else {
            Log.d(TAG, "Using existing timezone data (${timeZoneList.size} zones)")
        }
    }
    
    private fun updateLetterIndexMap() {
        letterToIndexMap.clear()
        timeZoneList.forEachIndexed { index, timeZone ->
            val startingLetter = timeZone.second.firstOrNull()?.toString()?.uppercase() ?: ""
            if (startingLetter !in letterToIndexMap) {
                letterToIndexMap[startingLetter] = index
            }
        }
    }

    /**
     * Fetches time zones and their offsets from WorldTimeAPI, populates list and index map.
     * Handles rate limiting gracefully by using fallback data when needed.
     */
    suspend fun fetchTimeZones() {
        withContext(Dispatchers.IO) {
            try {
                // First try to get the list of all zones
                Log.d(TAG, "Attempting to fetch timezone list from API")
                val zones = api.listZones()
                
                // Then fetch details for each zone
                val newTimeZones = fetchZoneDetails(zones)
                
                // Update the data if we got anything
                if (newTimeZones.isNotEmpty()) {
                    Log.d(TAG, "Successfully fetched ${newTimeZones.size} timezones from API")
                    updateTimeZoneData(newTimeZones)
                } else {
                    Log.w(TAG, "API returned empty timezone list, using fallback data")
                    // Ensure fallback data is used
                    initializeFallbackData()
                }
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    429 -> {
                        Log.w(TAG, "API rate limit exceeded (HTTP 429). Using fallback data", e)
                        // Make sure we have fallback data loaded
                        initializeFallbackData()
                    }
                    else -> {
                        Log.e(TAG, "HTTP error ${e.code()} fetching timezones. Using fallback data", e)
                        initializeFallbackData()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching timezones. Using fallback data", e)
                // Make sure we have fallback data loaded
                initializeFallbackData()
            }
        }
    }
    
    // Cache to store timezone data between app sessions
    private var lastFetchTime: Long = 0
    private val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    
    /**
     * Fetches timezone details in batches to avoid overwhelming the API
     * and triggering rate limits.
     */
    private suspend fun fetchZoneDetails(zones: List<String>): List<Triple<String, String, String>> {
        val result = mutableListOf<Triple<String, String, String>>()
        
        // Check if we need to fetch at all (use cache if available and recent)
        val currentTime = System.currentTimeMillis()
        if (timeZoneList.isNotEmpty() && currentTime - lastFetchTime < CACHE_DURATION_MS) {
            Log.d(TAG, "Using cached timezone data (${timeZoneList.size} zones)")
            return timeZoneList
        }
        
        // Process a limited number of important zones to avoid rate limiting
        val priorityZones = listOf(
            "America/New_York", "America/Los_Angeles", "America/Chicago", 
            "Europe/London", "Europe/Paris", "Asia/Tokyo", "Australia/Sydney", 
            "Etc/UTC"
        )
        
        // Only fetch the priority zones to avoid rate limiting
        val zonesToFetch = zones.filter { it in priorityZones }
        Log.d(TAG, "Fetching ${zonesToFetch.size} priority timezones instead of all ${zones.size} zones")
        
        // Process in small batches with delay between batches
        val batchSize = 2
        zonesToFetch.chunked(batchSize).forEach { batch ->
            batch.forEach { id ->
                try {
                    val resp: WorldTimeResponse = api.zoneInfo(id)
                    result.add(Triple(id, resp.timezone, resp.utc_offset))
                    Log.d(TAG, "Successfully fetched timezone: ${resp.timezone}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching zone $id", e)
                }
            }
            // Add a delay between batches to avoid rate limiting
            delay(1000) // 1 second delay between batches
        }
        
        // Update last fetch time
        if (result.isNotEmpty()) {
            lastFetchTime = currentTime
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
