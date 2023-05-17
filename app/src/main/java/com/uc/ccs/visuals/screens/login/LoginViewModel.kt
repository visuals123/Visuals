package com.uc.ccs.visuals.screens.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uc.ccs.visuals.utils.firebase.FirebaseAuthManager

class LoginViewModel : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState> = _authenticationState

    fun login(email: String, password: String) {
        val currentUser = FirebaseAuthManager.getCurrentUser()
        if (email.isBlank() || password.isBlank()) {
            _authenticationState.postValue(AuthenticationState.InvalidAuthentication)
            return
        }

        if (currentUser != null) {
            _authenticationState.value = AuthenticationState.Authenticated
        } else {
            // Perform login with Firebase Authentication
            FirebaseAuthManager.signInWithEmailAndPassword(email, password) { success ->
                if (success) {
                    _authenticationState.postValue(AuthenticationState.Authenticated)
                } else {
                    _authenticationState.postValue(AuthenticationState.InvalidAuthentication)
                }
            }
        }
    }
    sealed class AuthenticationState {
        object Authenticated : AuthenticationState()
        object InvalidAuthentication : AuthenticationState()
    }

}