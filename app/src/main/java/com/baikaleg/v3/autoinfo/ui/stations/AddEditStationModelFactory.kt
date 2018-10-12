package com.baikaleg.v3.autoinfo.ui.stations

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.baikaleg.v3.autoinfo.data.model.Route

class AddEditStationModelFactory(private val application: Application,
                                 private val route: Route,
                                 private val navigator: OnStationModelStateCallback) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditStationModel::class.java)) {
            return AddEditStationModel(application, route, navigator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}