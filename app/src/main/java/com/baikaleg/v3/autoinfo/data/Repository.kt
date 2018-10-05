package com.baikaleg.v3.autoinfo.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
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

    override fun updateStation(station: Station) {
        db.stationDao().updateStation(station)
    }

    override fun getRoutes(): LiveData<List<Route>> {
        return db.routeDao().getRoutes()
    }

    inner class saveAsync(val station: Station) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            db.stationDao().insertStation(station)
            return null
        }
    }
}
