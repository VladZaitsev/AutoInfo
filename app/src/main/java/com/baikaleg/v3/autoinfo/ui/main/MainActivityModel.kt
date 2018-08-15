package com.baikaleg.v3.autoinfo.ui.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import android.app.ActivityManager
import com.baikaleg.v3.autoinfo.service.StationUpdateService


class MainActivityModel(application: Application) : AndroidViewModel(application) {
    private val state: MutableLiveData<Boolean>? = null

    private val bcReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }


    init {
        LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(bcReceiver, IntentFilter(BROADCAST_ACTION))
    }

    companion object {
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

        fun create(activity: AppCompatActivity): MainActivityModel {
            return ViewModelProviders.of(activity).get(MainActivityModel::class.java)
        }
    }

    fun cancel() {
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(bcReceiver)
    }

    private fun startService() {
        getApplication<Application>().startService(Intent(getApplication(), StationUpdateService::class.java))
    }

    private fun stopService() {
        getApplication<Application>().stopService(Intent(getApplication(), StationUpdateService::class.java))
    }

    private fun isServiceRunning(): Boolean {
        val am: ActivityManager = getApplication<Application>()
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services: List<ActivityManager.RunningServiceInfo> = am.getRunningServices(Integer.MAX_VALUE)

        for (s in services) {
            if (s.service.className.equals(StationUpdateService::class)) {
                return true
            }
        }
        return false
    }
}