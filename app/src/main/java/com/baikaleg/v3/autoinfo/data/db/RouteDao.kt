package com.baikaleg.v3.autoinfo.data.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.baikaleg.v3.autoinfo.data.model.Route

@Dao
interface RouteDao {
    @Query("SELECT * FROM routeData")
    fun getRoutes(): LiveData<List<Route>>
}