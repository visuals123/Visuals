package com.uc.ccs.visuals.utils.firebase

import com.google.android.gms.maps.model.LatLng
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.models.TravelHistory
import com.uc.ccs.visuals.screens.settings.CsvData

interface IFirestoreRepository {
    fun saveMultipleData(collectionPath: String, data: List<CsvData>, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit)
    fun getCsvData(collectionPath: String, onSuccess: (List<CsvData>) -> Unit, onFailure: (e: Exception) -> Unit)
    fun getUsers(collectionPath: String, onSuccess: (List<UserItem>) -> Unit, onFailure: (e: Exception) -> Unit)
    fun getUserByEmail(collectionPath: String, email: String, onSuccess: (UserItem?) -> Unit, onFailure: (e: Exception) -> Unit)
    fun saveTravelRideHistory(
        collectionPath: String,
        userEmail: String,
        startDestinationName: String,
        endDestinationName: String,
        startDestinationLatLng: LatLng,
        endDestinationLatLng: LatLng,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )
    fun getTravelRideHistory(
        collectionPath: String,
        onSuccess: (List<TravelHistory>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun deleteTravelRideHistory(
        collectionPath: String,
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )
}