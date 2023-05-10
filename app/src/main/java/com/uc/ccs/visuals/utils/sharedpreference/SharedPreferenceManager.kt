package com.uc.ccs.visuals.utils.sharedpreference

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceManager {
    private const val PREF_NAME = "MyAppPreferences"
    private const val KEY_FIRST_LOGIN = "firstLogin"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLogin(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(KEY_FIRST_LOGIN, true)
    }

    fun setFirstLogin(context: Context, isFirstLogin: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_FIRST_LOGIN, isFirstLogin)
        editor.apply()
    }
}
