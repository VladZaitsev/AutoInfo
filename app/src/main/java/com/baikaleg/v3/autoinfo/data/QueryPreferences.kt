package com.baikaleg.v3.autoinfo.data

import android.content.Context
import android.preference.PreferenceManager

private const val PREF_ROUTE_QUERY = "route"
private const val PREF_CITY_QUERY = "city"
private const val PREF_ROUTES_LIST_QUERY = "routes_list"
private const val PREF_AUDIO_MODE_QUERY = "audio_mode"

class QueryPreferences(private val context: Context) {

    fun getRoute(): String {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PREF_ROUTE_QUERY, null)
    }

    fun setRoute(route: String) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_ROUTE_QUERY, route)
                .apply()
    }

    fun getCity(): String {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PREF_CITY_QUERY, null)
    }

    fun setCity(city: String) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_CITY_QUERY, city)
                .apply()
    }

    fun getAudioMode(): Int {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(PREF_AUDIO_MODE_QUERY, 0)
    }

    fun setAudioMode(mode: Int) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_AUDIO_MODE_QUERY, mode)
                .apply()
    }


}