package com.uc.ccs.visuals.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.screens.admin.AdminDashboardViewModel
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.main.history.HistoryViewModel

class AdminDashboardViewModelFactory(private val repository: CsvDataRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
            return AdminDashboardViewModel(repository) as T
        }else if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}