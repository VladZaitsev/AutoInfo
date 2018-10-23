package com.baikaleg.v3.autoinfo.ui.stations

import com.baikaleg.v3.autoinfo.data.model.Station
import com.google.android.gms.location.LocationRequest

interface OnStationModelStateCallback {

    fun onRecordBtnClicked(desc: String)

    fun onMessageReceived(message: String)

    fun onRecordPermissionRequest()

    fun onLocationPermissionRequest()

    fun onLocationSettingsRequest(locationRequest: LocationRequest)

    fun onStationChangedRequest(index:Int,station: Station)
}