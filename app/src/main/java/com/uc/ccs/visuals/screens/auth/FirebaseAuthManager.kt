package com.uc.ccs.visuals.screens.auth

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun getCurrentUser(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun signInWithEmailAndPassword(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User creation successful
                    callback(true)
                } else {
                    // User creation failed
                    callback(false)
                }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}