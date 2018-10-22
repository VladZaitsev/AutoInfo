package com.baikaleg.v3.autoinfo.ui.routes.dialog

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.model.Route

class AddEditRouteViewModel(application: Application) : AndroidViewModel(application) {

    private val route = MutableLiveData<Route>()
    private val repository = Repository.getInstance(application)

    init {
        route.postValue(Route("", false, "", listOf()))
    }

    fun setRoute(route: Route) {
        this.route.postValue(route)
    }

    fun getRoute(): Route? {
        return route.value
    }

    fun saveRoute() {
        repository.saveRoute(route.value!!)
    }

    fun isCircleParamChanged() {
        val state = route.value?.isCircle
        route.value?.isCircle = !state!!
    }

}