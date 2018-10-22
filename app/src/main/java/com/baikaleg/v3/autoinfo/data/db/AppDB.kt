package com.baikaleg.v3.autoinfo.data.db

import android.arch.persistence.room.*
import android.content.Context
import com.baikaleg.v3.autoinfo.data.converters.StationsConverter
import com.baikaleg.v3.autoinfo.data.model.Route


private const val DATABASE_NAME = "autoinfo.db"

@Database(entities = [Route::class], version = 1, exportSchema = false)
@TypeConverters(StationsConverter::class)
abstract class AppDB : RoomDatabase() {

    abstract fun routeDao(): RouteDao

    companion object {
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