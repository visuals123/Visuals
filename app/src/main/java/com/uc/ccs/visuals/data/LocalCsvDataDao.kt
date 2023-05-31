package com.uc.ccs.visuals.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalCsvDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCsvData(csvData: LocalCsvData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCsvDataList(csvDataList: List<LocalCsvData>)

    @Query("SELECT * FROM csv_data")
    suspend fun getAllCsvData(): List<LocalCsvData>

}