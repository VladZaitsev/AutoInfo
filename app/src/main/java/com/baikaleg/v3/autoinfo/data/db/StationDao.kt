package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.baikaleg.v3.autoinfo.data.model.Station
import io.reactivex.Flowable

@Dao
interface StationDao {

    @Query("SELECT DISTINCT city FROM stationData")
    fun getAllCities(): Flowable<List<String>>

    @Query("SELECT route FROM stationData WHERE city = :city")
    fun getAllRoutesInCity(city: String): Flowable<List<String>>

    @Query("SELECT * FROM stationData WHERE route = :route")
    fun getAllStationsInRoute(route: String?): Flowable<List<Station>>

    @Insert(onConflict = REPLACE)
    fun insert(station: Station)

    @Delete
    fun delete(station: Station)

}
