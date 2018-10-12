package com.baikaleg.v3.autoinfo.ui.stations

import com.google.android.gms.location.LocationRequest

interface OnStationModelStateCallback {

    fun onMessageReceived(message:String)

    fun onLocationPermissionRequest()

    fun onLocationSettingsRequest(locationRequest: LocationRequest)
}