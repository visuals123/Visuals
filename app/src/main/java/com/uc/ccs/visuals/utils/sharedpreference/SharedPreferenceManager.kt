package com.uc.ccs.visuals.utils.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.model.DirectionsRoute
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem

object SharedPreferenceManager {
    private const val PREF_NAME = "MyAppPreferences"
    private const val KEY_FIRST_LOGIN = "firstLogin"
    private const val KEY_USER_ID = "userId"
    private const val KEY_EMAIL = "userEmail"
    private const val KEY_USER_FIRST_NAME = "userFirstName"
    private const val KEY_USER_LAST_NAME = "userLastName"
    private const val KEY_USER_ROLES = "userRoles"
    private const val KEY_CURRENT_DIRECTION = "currentDirection"
    private const val KEY_CACHED_STARTING_POSITION_LAT = "cachedStartingPositionLat"
    private const val KEY_CACHED_STARTING_POSITION_LNG = "cachedStartingPositionLng"
    private const val KEY_CURRENT_DESTINATION_NAME = "currentDestinationName"
    private const val KEY_CURRENT_DESTINATION_LAT = "currentDestinationLat"
    private const val KEY_CURRENT_DESTINATION_LNG = "currentDestinationLng"

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

    fun cacheCurrentDirection(context: Context, directionsRoute: DirectionsRoute?) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        val json = if (directionsRoute != null) {
            Gson().toJson(directionsRoute)
        } else {
            null
        }
        editor.putString(KEY_CURRENT_DIRECTION, json)
        editor.apply()
    }

    fun getCurrentDirection(context: Context): DirectionsRoute? {
        val sharedPreferences = getSharedPreferences(context)
        val json = sharedPreferences.getString(KEY_CURRENT_DIRECTION, null)
        val type = object : TypeToken<DirectionsRoute>() {}.type
        val directionsRoute = if (json != null) {
            Gson().fromJson<DirectionsRoute>(json, type)
        } else {
            null
        }
        return directionsRoute
    }

    fun getCachedStartingPosition(context: Context): LatLng? {
        val sharedPreferences = getSharedPreferences(context)
        val lat = sharedPreferences.getFloat(KEY_CACHED_STARTING_POSITION_LAT, 0f)
        val lng = sharedPreferences.getFloat(KEY_CACHED_STARTING_POSITION_LNG, 0f)

        return if (lat != 0f && lng != 0f) {
            LatLng(lat.toDouble(), lng.toDouble())
        } else {
            null
        }
    }

    fun setCachedStartingPosition(context: Context, latLng: LatLng) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putFloat(KEY_CACHED_STARTING_POSITION_LAT, latLng.latitude.toFloat())
        editor.putFloat(KEY_CACHED_STARTING_POSITION_LNG, latLng.longitude.toFloat())
        editor.apply()
    }

    fun getCurrentDestinationName(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_CURRENT_DESTINATION_NAME, null)
    }

    fun setCurrentDestinationName(context: Context, name: String) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_CURRENT_DESTINATION_NAME, name)
        editor.apply()
    }

    fun getCurrentDestination(context: Context): LatLng? {
        val sharedPreferences = getSharedPreferences(context)
        val lat = sharedPreferences.getFloat(KEY_CURRENT_DESTINATION_LAT, 0f)
        val lng = sharedPreferences.getFloat(KEY_CURRENT_DESTINATION_LNG, 0f)

        return if (lat != 0f && lng != 0f) {
            LatLng(lat.toDouble(), lng.toDouble())
        } else {
            null
        }
    }

    fun setCurrentDestination(context: Context, latLng: LatLng) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putFloat(KEY_CURRENT_DESTINATION_LAT, latLng.latitude.toFloat())
        editor.putFloat(KEY_CURRENT_DESTINATION_LNG, latLng.longitude.toFloat())
        editor.apply()
    }

    fun clearCurrentDirection(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_CURRENT_DIRECTION)
        editor.apply()
    }

    fun clearCachedStartingPosition(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_CACHED_STARTING_POSITION_LAT)
        editor.remove(KEY_CACHED_STARTING_POSITION_LNG)
        editor.apply()
    }

    fun clearCurrentDestination(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_CURRENT_DESTINATION_LAT)
        editor.remove(KEY_CURRENT_DESTINATION_LNG)
        editor.apply()
    }

    fun clearCachedRide(context: Context) {
        clearCurrentDirection(context)
        clearCachedStartingPosition(context)
        clearCurrentDestination(context)
    }

}
