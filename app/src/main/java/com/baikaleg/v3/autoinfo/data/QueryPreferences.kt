package com.baikaleg.v3.autoinfo.data

import android.content.Context
import android.preference.PreferenceManager
import com.baikaleg.v3.autoinfo.R

private const val PREF_ROUTE_QUERY = "route"
private const val PREF_ANNOUNCE_AUDIO_TYPE_QUERY = "announce_audio_type"
private const val PREF_ANNOUNCE_STATION_TYPE_QUERY = "announce_station_type"

const val ANNOUNCE_AUDIO_TYPE_TTS = 0
const val ANNOUNCE_AUDIO_TYPE_PLAYER = 1

const val ANNOUNCE_STATION_TYPE_EMPTY = 0
const val ANNOUNCE_STATION_TYPE_CURRENT = 1
const val ANNOUNCE_STATION_TYPE_NEXT = 2
const val ANNOUNCE_STATION_TYPE_NEXT_WITH_DELAY = 3

class QueryPreferences(private val context: Context) {

    fun getRoute(): String {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PREF_ROUTE_QUERY, context.getString(R.string.route_is_not_selected))
    }

    fun setRoute(route: String) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_ROUTE_QUERY, route)
                .apply()
    }

    fun getAnnounceStationType(): Int {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(PREF_ANNOUNCE_STATION_TYPE_QUERY, 0)
    }

    fun setAnnounceStationType(type: Int) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_ANNOUNCE_STATION_TYPE_QUERY, type)
                .apply()
    }

    fun getAnnounceAudioType(): Int {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(PREF_ANNOUNCE_AUDIO_TYPE_QUERY, 0)
    }

    fun setAnnounceAudioType(mode: Int) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_ANNOUNCE_AUDIO_TYPE_QUERY, mode)
                .apply()
    }
}