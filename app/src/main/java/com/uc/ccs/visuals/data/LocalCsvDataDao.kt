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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: LocalHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(historyList: List<LocalHistory>)

    @Query("SELECT * FROM local_history")
    suspend fun getAllHistory(): List<LocalHistory>

}