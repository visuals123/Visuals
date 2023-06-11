package com.uc.ccs.visuals.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.uc.ccs.visuals.R

class GoogleSignInHelper() {

    private var gso: GoogleSignInOptions? = null
    var googleSignInClient: GoogleSignInClient? = null


    fun setupHelper(context: Context) {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        gso?.let {
            googleSignInClient = GoogleSignIn.getClient(context as Activity, it)
        }
    }

}
