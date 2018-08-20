package com.baikaleg.v3.autoinfo.ui.main

import android.Manifest
import android.app.ActivityManager
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import com.baikaleg.v3.autoinfo.service.StationSearchService

class MainActivityModel(application: Application) : AndroidViewModel(application) {

    private lateinit var navigator: OnPermissionRequest

    private val state = MutableLiveData<Boolean>()
    private val route = MutableLiveData<String>()

    fun getState(): LiveData<Boolean> {
        return state
    }

    fun getRoute(): LiveData<String> {
        return route
    }

    private val bcReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getIntExtra(PARAM_STATUS, 0)
            when (status) {
                STATUS_GPS -> route.postValue("gps ${intent.getDoubleExtra(PARAM_GPS,0.0)}")
            }
        }
    }

    init {
        val pref = QueryPreferences(getApplication())
        //route.postValue(pref.getRoute())
        route.postValue("test")
        if (isServiceRunning()) {
            state.postValue(false)
        } else {
            state.postValue(true)
        }

        getApplication<Application>().registerReceiver(bcReceiver, IntentFilter(BROADCAST_ACTION))
    }

    companion object {
        @JvmField
        val ROUTE_EXTRA = "route_extra"
        @JvmField
        val STATION_ANNOUNCEMENT_TYPE_EXTRA = "station_announcement_type_extra"

        @JvmField
        val BROADCAST_ACTION = "com.baikaleg.v3.autoinfo.br"

        @JvmField
        val STATUS_NEXT = 100
        @JvmField
        val STATUS_CURRENT = 200
        @JvmField
        val STATUS_GPS = 300
        @JvmField
        val STATUS_DIRECTION = 400

        @JvmField
        val PARAM_NEXT = "resultNext"
        @JvmField
        val PARAM_CURRENT = "resultCurrent"
        @JvmField
        val PARAM_GPS = "resultGPS"
        @JvmField
        val PARAM_DIRECTION = "resultDirection"
        @JvmField
        val PARAM_STATUS = "status"

        fun create(activity: AppCompatActivity): MainActivityModel {
            return ViewModelProviders.of(activity).get(MainActivityModel::class.java)
        }
    }

    fun onMainBtnClick(view: View) {
        if (isLocationControlAllowed()) {
            if (state.value == true) {
                state.postValue(false)
                startService()
            } else {
                state.postValue(true)
                stopService()
            }
        }
    }

    fun cancel() {
        getApplication<Application>().unregisterReceiver(bcReceiver)
    }

    fun setNavigator(nav: OnPermissionRequest) {
        navigator = nav
    }

    fun startService() {
        val intent = Intent(getApplication(), StationSearchService::class.java)
        intent.putExtra(ROUTE_EXTRA, route.value)
        getApplication<Application>().startService(intent)
    }

    private fun stopService() {
        getApplication<Application>().stopService(Intent(getApplication(), StationSearchService::class.java))
    }

    private fun isLocationControlAllowed(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                navigator.onLocationPermissionRequest()
                return false
            }
        }
        return true
    }

    private fun isServiceRunning(): Boolean {
        val am: ActivityManager = getApplication<Application>()
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services: List<ActivityManager.RunningServiceInfo> = am.getRunningServices(Integer.MAX_VALUE)

        for (s in services) {
            //TODO Change service name
            if (s.service.className.equals("com.baikaleg.v3.autoinfo.service.StationSearchService")) {
                return true
            }
        }
        return false
    }

    private fun checkGps(){
        //TODO Here realize gps check
    }
}