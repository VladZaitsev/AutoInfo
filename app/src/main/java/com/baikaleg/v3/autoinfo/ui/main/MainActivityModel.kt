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
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import com.baikaleg.v3.autoinfo.service.stationsearch.StationSearchNavigator
import com.baikaleg.v3.autoinfo.service.stationsearch.StationSearchService


const val ROUTE_EXTRA = "route_extra"
const val BROADCAST_ACTION = "com.baikaleg.v3.autoinfo.br"
const val PARAM_STATUS = "status"
const val STATUS_STATION = 101
const val STATUS_DIRECTION = 102
const val STATUS_GPS = 103
const val STATUS_MSG = 104

const val PARAM_STATION = "result_station"
const val PARAM_DIRECTION = "result_direction"
const val PARAM_GPS = "result_gps"
const val PARAM_MSG = "result_msg"

class MainActivityModel(application: Application) : AndroidViewModel(application) {

    private lateinit var navigator: StationSearchNavigator

    private val state = MutableLiveData<Boolean>()
    private val route = MutableLiveData<String>()
    private val station = MutableLiveData<String>()

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
                STATUS_STATION -> station.postValue(intent.getStringExtra(PARAM_STATION))
                STATUS_GPS -> route.postValue("gps ${intent.getDoubleExtra(PARAM_GPS, 0.0)}")
                STATUS_MSG -> navigator.onMessageReceived(intent.getStringExtra(PARAM_MSG))
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

    fun setNavigator(nav: StationSearchNavigator) {
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
            if (s.service.className.equals("com.baikaleg.v3.autoinfo.service.stationsearch.StationSearchService")) {
                return true
            }
        }
        return false
    }

    private fun checkGps() {
//TODO Here realize gps check
    }
}