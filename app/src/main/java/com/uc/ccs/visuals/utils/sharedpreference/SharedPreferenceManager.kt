package com.uc.ccs.visuals.utils.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem

object SharedPreferenceManager {
    private const val PREF_NAME = "MyAppPreferences"
    private const val KEY_FIRST_LOGIN = "firstLogin"
    private const val KEY_USER_ID = "userId"
    private const val KEY_EMAIL = "userEmail"
    private const val KEY_USER_FIRST_NAME = "userFirstName"
    private const val KEY_USER_LAST_NAME = "userLastName"
    private const val KEY_USER_ROLES = "userRoles"

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

    fun getCurrentUser(context: Context): UserItem? {
        val sharedPreferences = getSharedPreferences(context)
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val firstName = sharedPreferences.getString(KEY_USER_FIRST_NAME, null)
        val lastName = sharedPreferences.getString(KEY_USER_LAST_NAME, null)
        val roles = sharedPreferences.getInt(KEY_USER_ROLES, 0)

        return if (userId != null && firstName != null && lastName != null && email != null && roles != 0) {
            UserItem(
                id = userId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                roles = roles)
        } else {
            null
        }
    }

    fun setCurrentUser(context: Context, user: UserItem) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_USER_FIRST_NAME, user.firstName)
        editor.putString(KEY_USER_LAST_NAME, user.lastName)
        editor.putInt(KEY_USER_ROLES, user.roles)
        editor.apply()
    }

    fun clearCurrentUser(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_EMAIL)
        editor.remove(KEY_USER_FIRST_NAME)
        editor.remove(KEY_USER_LAST_NAME)
        editor.remove(KEY_USER_ROLES)
        editor.apply()
    }

    fun getRoles(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(KEY_USER_ROLES, 0)
    }
}
