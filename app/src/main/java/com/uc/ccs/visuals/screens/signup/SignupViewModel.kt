package com.uc.ccs.visuals.screens.signup

import User
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uc.ccs.visuals.screens.auth.FirebaseAuthManager

class SignupViewModel : ViewModel() {
    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState

    fun signupUser(user: User, password: String) {
        FirebaseAuthManager.createUserWithEmailAndPassword(user.email, password) { isSuccess ->
            if (isSuccess) {
                saveUserData(user)
            } else {
                _signupState.value = SignupState.Failure
            }
        }
    }

    private fun saveUserData(user: User) {
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection(COLLECTION_PATH)
            .add(user)
            .addOnSuccessListener { documentReference ->
                _signupState.value = SignupState.Success
            }
            .addOnFailureListener { e ->
                _signupState.value = SignupState.ErrorSavingData
            }
    }

    sealed class SignupState {
        object Success : SignupState()
        object Failure : SignupState()
        object ErrorSavingData : SignupState()
    }

    companion object {
        const val COLLECTION_PATH = "users"
    }

}

const val DIALOG_DURATION = 2000L