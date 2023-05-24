package com.uc.ccs.visuals.utils.firebase

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

    fun createUserWithEmailAndPassword(email: String, password: String, callback: (Boolean,Exception?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User creation successful
                    callback(true, null)
                } else {
                    // User creation failed
                    callback(false, task.exception)
                }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}