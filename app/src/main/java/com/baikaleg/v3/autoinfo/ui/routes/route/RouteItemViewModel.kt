package com.baikaleg.v3.autoinfo.ui.routes.route

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import com.baikaleg.v3.autoinfo.data.model.Route

class RouteItemViewModel(route: Route): BaseObservable() {
    @Bindable
    private var data = ObservableField<Route>()

    init {
        data.set(route)
    }

    fun getData(): ObservableField<Route> {
        return data
    }
}