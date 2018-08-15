package com.baikaleg.v3.autoinfo.ui.main

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
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.baikaleg.v3.autoinfo.service.StationUpdateService


class MainActivityModel(application: Application) : AndroidViewModel(application) {
    private val state = MutableLiveData<Boolean>()

    fun getState(): LiveData<Boolean> {
        return state
    }

    private val bcReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }


    init {
        if (isServiceRunning()) {
            state.postValue(true)
        } else {
            state.postValue(false)
        }
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

    fun onMainBtnClick(view: View) {
        if (state.value == true) {
            state.postValue(false)
        } else {
            state.postValue(true)
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