package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.*
import com.baikaleg.v3.autoinfo.data.model.Route
import io.reactivex.Flowable

@Dao
interface RouteDao {
    @Query("SELECT * FROM routeData")
    fun getRoutes(): Flowable<List<Route>>

    @Query("SELECT * FROM routeData where route = :route")
    fun getRoute(route: String): Flowable<Route>

    @Query("SELECT * FROM routeData where id = :id")
    fun getRoute(id: Long): Flowable<Route>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateRoute(station: Route)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoute(station: Route)

    @Delete
    fun deleteRoute(station: Route)
}