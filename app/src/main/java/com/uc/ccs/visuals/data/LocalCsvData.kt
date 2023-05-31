package com.uc.ccs.visuals.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "csv_data")
data class LocalCsvData(
    @PrimaryKey val id: String,
    val code: String,
    val title: String,
    val description: String,
    val position: String,
    val iconImageUrl: String?
)