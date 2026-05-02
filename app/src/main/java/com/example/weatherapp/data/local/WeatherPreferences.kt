package com.example.weatherapp.data.local

import android.content.Context
import android.content.SharedPreferences


class WeatherPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "weather_prefs"
        private const val KEY_LAST_CITY = "last_city"
        private const val KEY_IS_CELSIUS = "is_celsius"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val DEFAULT_CITY = "София"
    }


    fun saveLastCity(cityName: String) {
        prefs.edit().putString(KEY_LAST_CITY, cityName).apply()
    }


    fun getLastCity(): String {
        return prefs.getString(KEY_LAST_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
    }


    fun setIsCelsius(isCelsius: Boolean) {
        prefs.edit().putBoolean(KEY_IS_CELSIUS, isCelsius).apply()
    }


    fun isCelsius(): Boolean {
        return prefs.getBoolean(KEY_IS_CELSIUS, true)
    }


    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
}
