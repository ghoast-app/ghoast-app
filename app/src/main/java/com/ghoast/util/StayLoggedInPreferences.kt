package com.ghoast.utils

import android.content.Context
import android.content.SharedPreferences

object StayLoggedInPreferences {
    private const val PREF_NAME = "stay_logged_in_prefs"
    private const val KEY_STAY_LOGGED_IN = "stay_logged_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun save(context: Context, stayLoggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_STAY_LOGGED_IN, stayLoggedIn).apply()
    }

    fun loadStayLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_STAY_LOGGED_IN, false)
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
