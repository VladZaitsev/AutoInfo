package com.baikaleg.v3.autoinfo.ui.main

interface StationServiceNavigator {

    fun onLocationPermissionRequest()

    fun onMessageReceived(msg:String)

}