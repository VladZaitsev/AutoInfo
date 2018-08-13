package com.baikaleg.v3.autoinfo.data

import android.content.Context
import com.baikaleg.v3.autoinfo.data.db.AppDB
import com.baikaleg.v3.autoinfo.data.db.Station
import io.reactivex.disposables.CompositeDisposable

class Repository(context: Context) {
    private var db: AppDB? = AppDB.getInstance(context)
    private val cd: CompositeDisposable = CompositeDisposable()

    fun getStations(route: String): List<Station>? {
       return db?.stationDao()?.getAllStationsInRoute(route)?.map { t: List<Station> -> t}?.blockingFirst()?.toList()
    }

    fun close(){
        cd.clear()
    }

}
