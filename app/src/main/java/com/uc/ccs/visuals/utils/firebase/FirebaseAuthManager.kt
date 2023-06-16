package com.uc.ccs.visuals.utils.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthManager {
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun signInWithEmailAndPassword(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, callback: (Boolean, Exception?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // com.uc.ccs.visuals.screens.signup.User creation successful
                    callback(true, null)
                } else {
                    // com.uc.ccs.visuals.screens.signup.User creation failed
                    callback(false, task.exception)
                }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun sendEmailVerification(email: String, callback: (Boolean) -> Unit) {
        val currentUser = firebaseAuth.currentUser

        currentUser?.let { user ->
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful)
                }
        } ?: run {
            callback(false)
        }
    }

}
