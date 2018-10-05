package com.baikaleg.v3.autoinfo.ui.stations

import com.google.android.gms.location.LocationRequest

interface OnStationChangeNavigator {

    fun onLocationPermissionRequest()

    fun onLocationSettingsRequest(locationRequest: LocationRequest)
}