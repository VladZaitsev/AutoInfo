package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.baikaleg.v3.autoinfo.data.model.Station

@Database(entities = arrayOf(Station::class), version = 1)
abstract class AppDB : RoomDatabase() {

    abstract fun stationDao(): StationDao

    companion object {
        private val DATABASE_NAME = "autoinfo.db"
        private var INSTANCE: AppDB? = null

        fun getInstance(context: Context): AppDB? {
            if (INSTANCE == null) {
                synchronized(AppDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AppDB::class.java,
                            DATABASE_NAME)
                            .allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE
        }
    }

}