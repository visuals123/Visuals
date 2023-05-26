package com.uc.ccs.visuals.screens.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.settings.CsvData

class AdminDashboardViewModel : ViewModel() {

    private val _users = MutableLiveData<List<UserItem>>()
    val users: LiveData<List<UserItem>> get() = _users

    private val _signs = MutableLiveData<List<CsvData>>()
    val signs: LiveData<List<CsvData>> get() = _signs

    fun setUsers(users: List<UserItem>) {
        _users.value = users
    }

    fun setCsvData(users: List<CsvData>) {
        _signs.value = users
    }

}