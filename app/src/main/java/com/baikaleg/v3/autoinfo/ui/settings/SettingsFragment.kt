package com.baikaleg.v3.autoinfo.ui.settings

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.QueryPreferences

class SettingsFragment : PreferenceFragment() {

    private lateinit var pref: QueryPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

        pref = QueryPreferences(activity)
        bindPreferenceSummaryToValue(findPreference(getString(R.string.audio_type_list_key)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.station_type_list_key)))
    }

    private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(
                    if (index >= 0) preference.entries[index]
                    else
                        null)
            val key = preference.key
            when (key) {
                getString(R.string.audio_type_list_key) -> {
                    pref.setAnnounceAudioType(index)
                }
                getString(R.string.station_type_list_key) -> {
                    pref.setAnnounceStationType(index)
                }
            }
        }
        true
    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getString(preference.key, ""))
    }
}