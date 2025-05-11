package com.example.worktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var date: String,
    var shiftSpan: String,
    val breakTotal: String,
    val shiftTotal: String,
    // Weather information
    val weatherTemp: Double? = null,
    val weatherDescription: String? = null,
    val weatherIcon: String? = null,
    val weatherLocation: String? = null
)