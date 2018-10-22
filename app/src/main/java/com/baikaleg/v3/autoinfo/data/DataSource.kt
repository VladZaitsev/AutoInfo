package com.baikaleg.v3.autoinfo.data

import com.baikaleg.v3.autoinfo.data.model.Route
import io.reactivex.Flowable

interface DataSource {

    fun getRoutes(): Flowable<List<Route>>

    fun getRoute(id: Long): Flowable<Route>

    fun getRoute(stg: String): Flowable<Route>

    fun saveRoute(route: Route)

    fun deleteRoute(route: Route)

    fun updateRoute(route: Route)
}