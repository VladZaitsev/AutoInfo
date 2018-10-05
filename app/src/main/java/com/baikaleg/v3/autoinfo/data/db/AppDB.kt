package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station

@Database(entities = arrayOf(Station::class, Route::class), version = 1, exportSchema = false)
abstract class AppDB : RoomDatabase() {

    abstract fun stationDao(): StationDao

    abstract fun routeDao(): RouteDao

    companion object {
        private val DATABASE_NAME = "autoinfo.db"
        @Volatile private var instance: AppDB? = null

        fun getInstance(context: Context): AppDB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext,
                        AppDB::class.java,
                        DATABASE_NAME)
                        .build()
                        .also { instance = it }
            }
        }
    }
}