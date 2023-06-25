package com.uc.ccs.visuals.screens.main.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.data.LocalHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: CsvDataRepository) : ViewModel() {

    private val _localAllHistory = MutableLiveData<List<LocalHistory>>()
    val localAllHistory: LiveData<List<LocalHistory>> get() = _localAllHistory

    private val _selectedHistory = MutableLiveData<LocalHistory>()
    val selectedHistory: LiveData<LocalHistory> get() = _selectedHistory

    private val _isFromHistory = MutableLiveData(false)
    val isFromHistory: LiveData<Boolean> get() = _isFromHistory

    fun setAllLocalHistory(list: List<LocalHistory>) {
        _localAllHistory.value = list
    }

    fun setIsFromHistory(bool: Boolean) {
        _isFromHistory.value = bool
    }

    fun setSelectedHistory(item: LocalHistory) {
        _selectedHistory.value = item
    }

    fun saveHistoryToLocal(history: LocalHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertHistory(history)
        }
    }

    fun saveHistoryListToLocal(history: List<LocalHistory>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertHistoryList(history)
        }
    }

    fun getLocalHistory() {
        viewModelScope.launch {
            val allHistory = repository.getAllHistory()
            _localAllHistory.value = allHistory
        }
    }

}