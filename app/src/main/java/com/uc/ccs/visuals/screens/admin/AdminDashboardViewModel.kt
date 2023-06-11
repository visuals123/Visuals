package com.uc.ccs.visuals.screens.admin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.data.LocalCsvData
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.settings.CsvData
import com.uc.ccs.visuals.utils.extensions.toLocalCsvDataList
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val repository: CsvDataRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<UserItem>>()
    val users: LiveData<List<UserItem>> get() = _users

    private val _signs = MutableLiveData<List<CsvData>>()
    val signs: LiveData<List<CsvData>> get() = _signs

    private val _localSigns = MutableLiveData<List<LocalCsvData>>()
    val localSigns: LiveData<List<LocalCsvData>> get() = _localSigns

    private val _insertCsvDataToLocalDbState = MutableLiveData<LocalDBState>()
    val insertCsvDataToLocalDbState: LiveData<LocalDBState> get() = _insertCsvDataToLocalDbState

    private val _csvExportDataState = MutableLiveData<CsvExportDataState>()
    val csvExportDataState: LiveData<CsvExportDataState> get() = _csvExportDataState

    val csvClient = CSVClient()
    private var context: Context? = null

    fun setUsers(users: List<UserItem>) {
        _users.value = users
    }

    fun setContext(context: Context) {
        this.context= context
    }

    fun setCsvData(users: List<CsvData>) {
        _signs.value = users
    }

    fun insertCsvDataToLocalDb(csvData: List<CsvData>) {
        viewModelScope.launch {
            try {
                repository.insertCsvDataList(csvData.toLocalCsvDataList())
                _insertCsvDataToLocalDbState.value = LocalDBState.Success(csvData)
            } catch (e: Exception) {
                _insertCsvDataToLocalDbState.value = LocalDBState.Error(e)
            }
        }
    }

    fun loadCsvData() {
        viewModelScope.launch {
            try {
                val csvData = repository.getAllCsvData()
                _localSigns.value = csvData
            } catch (e: Exception) {
                // Handle the error appropriately
            }
        }
    }

    fun exportMarkersToCSV() {
        val markerInfos = _signs.value
        markerInfos?.let {
            _csvExportDataState.value = CsvExportDataState.onLoad
            val csvData = csvClient.convertMarkerInfoToCSV(markerInfos)
            csvClient.exportToCSV(context!!,"visuals_markers.csv", csvData, {
                _csvExportDataState.value = CsvExportDataState.onSuccess
            }, {
                _csvExportDataState.value = CsvExportDataState.onFailure(it.localizedMessage.toString())
            })

        } ?: run {
            _csvExportDataState.value = CsvExportDataState.onFailure(NO_DATA_AVAILABLE)
        }
    }

    fun restoreSavedUri(context: Context) {
        csvClient.restoreSavedUri(context)
    }

    fun exportUsersToCSV() {
        val users = _users.value
        users?.let {
            _csvExportDataState.value = CsvExportDataState.onLoad
            val csvData = csvClient.convertUserItemToCSV(users)
            csvClient.exportToCSV(context!!,"visuals_users.csv", csvData, {
                _csvExportDataState.value = CsvExportDataState.onSuccess
            }, {
                _csvExportDataState.value = CsvExportDataState.onFailure(it.localizedMessage?.toString() ?: "Something went wrong")
            })

        } ?: run {
            _csvExportDataState.value = CsvExportDataState.onFailure(NO_DATA_AVAILABLE)
        }
    }

}

const val NO_DATA_AVAILABLE = "No data available!"

sealed class LocalDBState {
    object Loading : LocalDBState()
    data class Success(val csvData: List<CsvData>) : LocalDBState()
    data class Error(val exception: Exception) : LocalDBState()
}

sealed class CsvExportDataState {
    object onLoad: CsvExportDataState()
    object onSuccess: CsvExportDataState()
    data class onFailure(val e: String): CsvExportDataState()

}