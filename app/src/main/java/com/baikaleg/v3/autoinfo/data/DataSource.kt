package com.baikaleg.v3.autoinfo.data

import android.arch.lifecycle.LiveData
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import io.reactivex.Flowable

interface DataSource {

    fun getStations(route: String): Flowable<List<Station>>

    fun saveStation(station: Station)

    fun updateStation(station: Station)

    fun getRoutes(): LiveData<List<Route>>
}