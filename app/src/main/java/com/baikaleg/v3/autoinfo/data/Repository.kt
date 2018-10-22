package com.baikaleg.v3.autoinfo.data

import android.content.Context
import android.os.AsyncTask
import com.baikaleg.v3.autoinfo.data.db.AppDB
import com.baikaleg.v3.autoinfo.data.model.Route
import io.reactivex.Flowable

class Repository private constructor(context: Context) : DataSource {

    private val db: AppDB = AppDB.getInstance(context)

    companion object {

        @Volatile
        private var instance: Repository? = null

        fun getInstance(context: Context) =
                instance ?: synchronized(this) {
                    instance ?: Repository(context).also { instance = it }
                }
    }

    override fun getRoutes(): Flowable<List<Route>> {
        return db.routeDao().getRoutes()
    }

    override fun getRoute(id: Long): Flowable<Route> {
        return db.routeDao().getRoute(id)
    }

    override fun getRoute(stg: String): Flowable<Route> {
        return db.routeDao().getRoute(stg)
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
            }
            return null
        }
    }

    private inner class deleteAsync<T>(private val entity: T) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            if (entity is Route) {
                db.routeDao().deleteRoute(entity)
            }
            return null
        }
    }

    private inner class updateAsync<T>(private val entity: T) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            if (entity is Route) {
                db.routeDao().updateRoute(entity)
            }
            return null
        }
    }
}
