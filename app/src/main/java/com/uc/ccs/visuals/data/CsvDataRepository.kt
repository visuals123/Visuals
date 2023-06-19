package com.uc.ccs.visuals.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CsvDataRepository(context: Context) {
    private val localCsvDataDao: LocalCsvDataDao

    init {
        val database = AppDatabase.getDatabase(context)
        localCsvDataDao = database.localCsvDataDao()
    }

    suspend fun insertCsvData(csvData: LocalCsvData) {
        withContext(Dispatchers.IO) {
            localCsvDataDao.insertCsvData(csvData)
        }
    }

    suspend fun insertCsvDataList(csvData: List<LocalCsvData>) {
        withContext(Dispatchers.IO) {
            localCsvDataDao.insertCsvDataList(csvData)
        }
    }

    suspend fun getAllCsvData(): List<LocalCsvData> {
        return withContext(Dispatchers.IO) {
            localCsvDataDao.getAllCsvData()
        }
    }

    suspend fun insertHistory(history: LocalHistory) {
        localCsvDataDao.insertHistory(history)
    }

    suspend fun insertHistoryList(historyList: List<LocalHistory>) {
        localCsvDataDao.insertHistoryList(historyList)
    }

    suspend fun getAllHistory(): List<LocalHistory> {
        return localCsvDataDao.getAllHistory()
    }

}