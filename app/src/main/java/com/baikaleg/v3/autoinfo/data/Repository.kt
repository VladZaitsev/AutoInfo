package com.baikaleg.v3.autoinfo.data

import android.content.Context
import android.os.AsyncTask
import com.baikaleg.v3.autoinfo.R.string.station
import com.baikaleg.v3.autoinfo.data.db.AppDB
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import io.reactivex.Flowable

class Repository private constructor(context: Context) : DataSource {
    private val db: AppDB = AppDB.getInstance(context)

    companion object {

        @Volatile private var instance: Repository? = null

        fun getInstance(context: Context) =
                instance ?: synchronized(this) {
                    instance ?: Repository(context).also { instance = it }
                }
    }

    override fun getStations(route: String): Flowable<List<Station>> {
        return db.stationDao().getStations(route)
    }

    override fun saveStation(station: Station) {
        saveAsync(station).execute()
    }

    override fun deleteStation(station: Station) {
        deleteAsync(station).execute()
    }

    override fun updateStation(station: Station) {
        updateAsync(station).execute()
    }

    override fun getRoutes(): Flowable<List<Route>> {
        return db.routeDao().getRoutes()
    }


    override fun saveRoute(route: Route) {
        saveAsync(route).execute()
    }

    override fun deleteRoute(route: Route) {
        deleteAsync(route).execute()
    }

    override fun updateRoute(route: Route) {
        updateAsync(route).execute()
    }

    private inner class saveAsync<T>(private val entity: T) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            if (entity is Route) {
                db.routeDao().insertRoute(entity)
            } else if (entity is Station) {
                db.stationDao().insertStation(entity)
            }
            return null
        }
    }

    private inner class deleteAsync<T>(private val entity: T) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            if (entity is Route) {
                db.routeDao().deleteRoute(entity)
            } else if (entity is Station) {
                db.stationDao().deleteStation(entity)
            }
            return null
        }
    }

    private inner class updateAsync<T>(private val entity: T) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            if (entity is Route) {
                db.routeDao().updateRoute(entity)
            } else if (entity is Station) {
                db.stationDao().updateStation(entity)
            }
            return null
        }
    }
}
