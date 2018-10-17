package com.baikaleg.v3.autoinfo.data

import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import io.reactivex.Flowable

interface DataSource {

    fun getStations(route: String): Flowable<List<Station>>

    fun saveStation(station: Station)

    fun updateStation(station: Station)

    fun deleteStation(station: Station)

    fun getRoutes(): Flowable<List<Route>>

    fun saveRoute(route: Route)

    fun deleteRoute(route: Route)

    fun updateRoute(route: Route)
}