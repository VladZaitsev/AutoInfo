package com.baikaleg.v3.autoinfo.service.stationsearch

interface StationSearchNavigator {

    fun onLocationPermissionRequest()

    fun onMessageReceived(msg:String)

}