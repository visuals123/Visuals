package com.uc.ccs.visuals.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_history")
data class LocalHistory(
    @PrimaryKey
    val id: String,
    val email: String,
    val startDestinationName: String,
    val endDestinationName: String,
    val startDestinationLatLng: String,
    val endDestinationLatLng: String
)