package com.example.worktracker.mock

import com.example.worktracker.Constants.BREAK_START_KEY
import com.example.worktracker.Constants.BREAK_TOTAL_KEY
import com.example.worktracker.Constants.CLOCKED_IN_KEY
import com.example.worktracker.Constants.ON_BREAK_KEY
import com.example.worktracker.Constants.SHIFT_START_KEY
import com.example.worktracker.Constants.START_OF_WEEK_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY


import com.example.worktracker.data.SharedPreferencesRepository

class SharedPreferencesMock : SharedPreferencesRepository {
    private var clockedIn: Boolean? = null
    private var onBreak: Boolean? = null
    private var breakTotal: String? = null
    private var shiftStart: String? = null
    private var breakStart: String? = null

    private var timeZone = "Etc/UTC"
    private var startOfWeek = "SUNDAY"

    override fun getString(key: String, defaultValue: String): String {
        return when(key) {
            BREAK_TOTAL_KEY -> breakTotal ?: defaultValue
            SHIFT_START_KEY -> shiftStart ?: defaultValue
            BREAK_START_KEY -> breakStart ?: defaultValue
            TIME_ZONE_KEY -> timeZone
            START_OF_WEEK_KEY -> startOfWeek
            else -> ""
        }
    }
    override fun putString(key: String, value: String) {
        when(key) {
            BREAK_TOTAL_KEY -> breakTotal = value
            SHIFT_START_KEY -> shiftStart = value
            BREAK_START_KEY -> breakStart = value
            START_OF_WEEK_KEY -> startOfWeek = value
        }
    }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return when(key) {
            CLOCKED_IN_KEY -> clockedIn ?: defaultValue
            ON_BREAK_KEY -> onBreak ?: defaultValue
            else -> false
        }
    }
    override fun putBoolean(key: String, value: Boolean) {
        when(key) {
            CLOCKED_IN_KEY -> clockedIn = value
            ON_BREAK_KEY -> onBreak = value
        }
    }
    override fun remove(key: String) {
        when(key) {
            CLOCKED_IN_KEY -> clockedIn = null
            ON_BREAK_KEY -> onBreak = null
            SHIFT_START_KEY -> shiftStart = null
            BREAK_TOTAL_KEY -> breakTotal = null
            BREAK_START_KEY -> breakStart = null
        }
    }
}