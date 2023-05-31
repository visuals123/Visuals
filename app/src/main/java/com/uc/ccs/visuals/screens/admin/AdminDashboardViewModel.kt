package com.uc.ccs.visuals.screens.admin

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


    fun setUsers(users: List<UserItem>) {
        _users.value = users
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

}

sealed class LocalDBState {
    object Loading : LocalDBState()
    data class Success(val csvData: List<CsvData>) : LocalDBState()
    data class Error(val exception: Exception) : LocalDBState()
}