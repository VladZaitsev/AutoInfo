package com.baikaleg.v3.autoinfo.ui.main

interface StationSearchNavigator {

    fun onLocationPermissionRequest()

    fun onMessageReceived(msg: String)

    fun onLocationSettingsRequest()

    fun isGooglePlayServicesAvailable(): Boolean

    fun onServiceStateChanged(isRunning:Boolean)

}