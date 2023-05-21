package com.uc.ccs.visuals.utils.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uc.ccs.visuals.screens.settings.CsvData

class FirestoreViewModel : ViewModel() {
    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    private val repository = FirestoreRepository()

    fun saveCsvData(data: List<CsvData>) {
        _operationState.value = OperationState.Loading
        repository.saveMultipleData(ROAD_SIGN_COLLECTION_PATH, data, {
            _operationState.value = OperationState.Success
        }, {
            _operationState.value = OperationState.Error(it)
        })
    }

    fun retrieveData(onSuccess: (List<CsvData>) -> Unit, onFailure: (e: Exception) -> Unit) {
        repository.getCsvData(ROAD_SIGN_COLLECTION_PATH,onSuccess,onFailure)
    }

    companion object {
        const val ROAD_SIGN_COLLECTION_PATH = "roadsigns"
    }

    // You can add more functions for other Firestore operations like update, delete, etc.

    sealed class OperationState {
        object Loading : OperationState()
        object Success : OperationState()
        data class Error(val exception: Exception) : OperationState()
    }
}