package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.baikaleg.v3.autoinfo.data.model.Station
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface StationDao {

    @Query("SELECT DISTINCT city FROM stationData")
    fun getAllCities(): Flowable<List<String>>

    @Query("SELECT route FROM stationData WHERE city = :city")
    fun getAllRoutesInCity(city: String): Flowable<List<String>>

    @Query("SELECT * FROM stationData WHERE route = :route ORDER BY order_number")
    fun getStations(route: String?): Flowable<List<Station>>

    @Query("SELECT * FROM stationData where id = :id")
    fun getStation(id: Int): Station

    @Update(onConflict = REPLACE)
    fun updateStation(station: Station)

    @Insert(onConflict = REPLACE)
    fun insertStation(station: Station)

    @Delete
    fun deleteStation(station: Station)
}
