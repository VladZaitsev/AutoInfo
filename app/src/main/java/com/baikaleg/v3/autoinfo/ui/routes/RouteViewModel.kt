package com.baikaleg.v3.autoinfo.ui.routes

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.exportimport.ExportImportRoute
import com.baikaleg.v3.autoinfo.data.model.Route
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * The ViewModel for [RouteActivity]
 */
class RouteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository.getInstance(application)

    val routes = MutableLiveData<List<Route>>()
    private val compositeDisposable = CompositeDisposable()
    private val exportImportRoute = ExportImportRoute(application)

    init {
        load()
    }

    private fun load() {
        compositeDisposable.add(repository.getRoutes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data: List<Route> -> routes.postValue(data) })
    }

    fun onRemove(position: Int) {
        repository.deleteRoute(routes.value!![position])
    }

    fun importRoute(fileName: String) {
        exportImportRoute.importFromFile(fileName)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        exportImportRoute.clear()
    }
}