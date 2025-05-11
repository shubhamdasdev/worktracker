package com.example.worktracker.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants
import com.example.worktracker.Constants.BREAK_START_KEY
import com.example.worktracker.Constants.BREAK_TOTAL_KEY
import com.example.worktracker.Constants.CLOCKED_IN_KEY
import com.example.worktracker.Constants.ON_BREAK_KEY
import com.example.worktracker.Constants.SHIFT_START_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.TAG
import com.example.worktracker.Utils.getBreakCounter
import com.example.worktracker.Utils.getCounter
import com.example.worktracker.Utils.getDisplayTimeAtTimeZone
import com.example.worktracker.Utils.getTimeDiff
import com.example.worktracker.Utils.getTimeStamp
import com.example.worktracker.Utils.subtractBreakFromTotal
import com.example.worktracker.data.SharedPreferencesRepository
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import com.example.worktracker.data.WeatherRepository
import com.example.worktracker.network.WeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.*


data class MainUiState(
    val clockedIn: Boolean,
    val onBreak: Boolean,
    val shiftStartTime: String,
    val breakStartTime: String,
    val breakTotal: String,
    val counter: String,
    val breakCounter: String,
    val weatherEnabled: Boolean = false
)

class MainViewModel(
    private val shiftsRepository: ShiftsRepository,
    private val sharedPref: SharedPreferencesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState>
    val uiState: StateFlow<MainUiState>
    
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState

    private val selectedTimeZone: ZoneId
    private var counterJob: Job? = null
    private var currentWeatherData: WeatherData? = null

    init {
        val timeZoneString = sharedPref.getString(TIME_ZONE_KEY, "Etc/UTC")
        selectedTimeZone = ZoneId.of(timeZoneString)

        val clockedIn = sharedPref.getBoolean(CLOCKED_IN_KEY, false)
        val onBreak = sharedPref.getBoolean(ON_BREAK_KEY, false)

        val timestampStart = sharedPref.getString(SHIFT_START_KEY, "error")
        val shiftStartTime = if (timestampStart != "error") getDisplayTimeAtTimeZone(selectedTimeZone, timestampStart) else "error"

        val timestampBreak = sharedPref.getString(BREAK_START_KEY, "error")
        val breakStartTime = if (timestampBreak != "error") getDisplayTimeAtTimeZone(selectedTimeZone, timestampBreak) else "error"

        val breakTotal = sharedPref.getString(BREAK_TOTAL_KEY, "0:00")
        val counter = getCounter()
        val breakCounter = getBreakCounter()
        val weatherEnabled = sharedPref.getBoolean(Constants.WEATHER_ENABLED_KEY, false)

        _uiState = MutableStateFlow(
            MainUiState(
                clockedIn,
                onBreak,
                shiftStartTime,
                breakStartTime,
                breakTotal,
                counter,
                breakCounter,
                weatherEnabled
            )
        )
        uiState = _uiState.asStateFlow()

        if (clockedIn) {
            startCounter()
        }
        
        // Initialize weather if enabled
        if (weatherEnabled) {
            refreshWeather()
        }
    }

    private fun startCounter() {
        counterJob?.cancel()
        counterJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                Log.d(TAG, "run")
                _uiState.update {
                    it.copy(
                        counter = getCounter(),
                        breakCounter = getBreakCounter()
                    )
                }
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, 1)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val delay = calendar.timeInMillis - System.currentTimeMillis()
                delay(delay)
            }
        }
    }

    private fun stopCounter() {
        counterJob?.cancel()
    }

    fun updateClockedIn() {
        val clockedIn = uiState.value.clockedIn

        if (!clockedIn) { //clocking in
            //2023.01.22.12:12 PM
            val timestamp = getTimeStamp()

            val shiftStartTimeUser = getDisplayTimeAtTimeZone(selectedTimeZone, timestamp)

            sharedPref.putBoolean(CLOCKED_IN_KEY, true)
            sharedPref.putString(SHIFT_START_KEY, timestamp)

            _uiState.update { currentState ->
                currentState.copy(
                    clockedIn = true,
                    shiftStartTime = shiftStartTimeUser
                )
            }
            startCounter()
        } else { // clocking out
            if (uiState.value.onBreak){
                updateOnBreak()
            }
            val startTime = sharedPref.getString(SHIFT_START_KEY, "")
            val endTime = getTimeStamp()

            createShiftAndInsert(startTime, endTime)

            sharedPref.remove(CLOCKED_IN_KEY)
            sharedPref.remove(BREAK_TOTAL_KEY)
            sharedPref.remove(SHIFT_START_KEY)

            _uiState.update { currentState ->
                currentState.copy(
                    clockedIn = false,
                )
            }
            stopCounter()
        }
    }

    private fun createShiftAndInsert(startTimeStamp: String, endTimeStamp: String) {
        //2023.01.22.12:12 PM
        //2023.01.22.12:12 PM

        val date = startTimeStamp.substring(0, 10)

        val breakTime = sharedPref.getString(BREAK_TOTAL_KEY, "0:00")

        val shiftLength = getTimeDiff(startTimeStamp, endTimeStamp)
        val shiftTotal = subtractBreakFromTotal(breakTime, shiftLength)

        val timeStart = if (startTimeStamp[11] == '0') startTimeStamp.substring(12) else startTimeStamp.substring(11)
        val timeEnd = if (endTimeStamp[11] == '0') endTimeStamp.substring(12) else endTimeStamp.substring(11)

        // Include weather data if available and enabled
        val weatherEnabled = sharedPref.getBoolean(Constants.WEATHER_ENABLED_KEY, false)
        val weatherData = if (weatherEnabled) currentWeatherData else null
        
        val shift = Shift(
            date = date,
            shiftSpan = "$timeStart - $timeEnd",
            breakTotal = breakTime,
            shiftTotal = shiftTotal,
            weatherTemp = weatherData?.temperature,
            weatherDescription = weatherData?.description,
            weatherIcon = weatherData?.icon,
            weatherLocation = weatherData?.location
        )

        viewModelScope.launch {
            shiftsRepository.insertItem(shift)
        }
    }

    fun updateOnBreak() {
        val onBreak = uiState.value.onBreak

        if (!onBreak) {
            val timestamp = getTimeStamp()
            val breakStartTime = getDisplayTimeAtTimeZone(selectedTimeZone, timestamp)

            sharedPref.putBoolean(ON_BREAK_KEY, true)
            sharedPref.putString(BREAK_START_KEY, timestamp)

            _uiState.update { currentState ->
                currentState.copy(
                    onBreak = true,
                    breakStartTime = breakStartTime,
                )
            }
        } else {
            val timeStart = sharedPref.getString(BREAK_START_KEY, "")
            val timeEnd = getTimeStamp()

            val breakTotal = getTimeDiff(timeStart, timeEnd)

            sharedPref.putString(BREAK_TOTAL_KEY, breakTotal)
            sharedPref.remove(ON_BREAK_KEY)
            sharedPref.remove(BREAK_START_KEY)

            _uiState.update { currentState ->
                currentState.copy(
                    onBreak = false,
                    breakTotal = breakTotal
                )
            }
        }
    }

    suspend fun fetchShiftData(): List<Shift> {
        return shiftsRepository.getAllItems()
    }

    fun updateView() {
        _uiState.update { currentState ->
            currentState.copy(
                counter = getCounter(),
                breakCounter = getBreakCounter()
            )
        }
    }
    
    /**
     * Refresh weather data using location
     */
    fun refreshWeather(context: android.content.Context? = null) {
        if (!uiState.value.weatherEnabled) {
            _weatherState.value = WeatherState.Loading
            return
        }
        
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            // Check if we have location permission
            context?.let {
                val location = weatherRepository.getLastKnownLocation(it)
                if (location == null) {
                    _weatherState.value = WeatherState.PermissionRequired
                    return@launch
                }
                
                // Get weather data for the location
                val result = weatherRepository.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude
                )
                
                // Handle the result
                if (result.isSuccess) {
                    val weatherData = result.getOrNull()
                    if (weatherData != null) {
                        currentWeatherData = weatherData
                        _weatherState.value = WeatherState.Success(weatherData)
                    } else {
                        _weatherState.value = WeatherState.Error("No weather data available")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    _weatherState.value = WeatherState.Error(error?.message ?: "Unknown error")
                }
            } ?: run {
                _weatherState.value = WeatherState.Error("Context not available")
            }
        }
    }
    
    /**
     * Request location permission for weather
     */
    fun requestWeatherPermission() {
        _weatherState.value = WeatherState.PermissionRequired
    }
}
