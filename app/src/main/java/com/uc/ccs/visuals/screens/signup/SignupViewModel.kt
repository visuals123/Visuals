package com.uc.ccs.visuals.screens.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager

class SignupViewModel : ViewModel() {
    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState

    fun signupUser(user: User, password: String) {
        FirebaseAuthManager.createUserWithEmailAndPassword(user.email, password) { isSuccess, err ->
            if (isSuccess) {
                saveUserData(user)
            } else {
                _signupState.value = SignupState.Failure(err ?: Exception())
            }
        }
    }

    private fun saveUserData(user: User) {
        val firebaseFirestore = FirebaseFirestore.getInstance()

        val email = user.email
        sendEmailVerification(email)

        firebaseFirestore.collection(COLLECTION_PATH)
            .document(email)
            .set(user)
            .addOnSuccessListener { documentReference ->
                _signupState.value = SignupState.Success
            }
            .addOnFailureListener { e ->
                _signupState.value = SignupState.ErrorSavingData
            }
    }

    private fun sendEmailVerification(email: String) {
        FirebaseAuthManager.sendEmailVerification(email) { isEmailSent ->
            if (isEmailSent) {
                _signupState.value = SignupState.EmailVerificationSent
            } else {
                _signupState.value = SignupState.ErrorSendingVerification
            }
        }
    }

    // Call this function when email verification is completed by the user
    /*
    fun completeEmailVerification() {
        _signupState.value = SignupState.EmailVerificationCompleted
    }
     */


    sealed class SignupState {
        object Success : SignupState()
        data class Failure(val error: Exception) : SignupState()
        object ErrorSavingData : SignupState()
        object ErrorSendingVerification : SignupState()
        object EmailVerificationSent : SignupState()
    }

    companion object {
        const val COLLECTION_PATH = "users"
    }

}

const val DIALOG_DURATION = 500L