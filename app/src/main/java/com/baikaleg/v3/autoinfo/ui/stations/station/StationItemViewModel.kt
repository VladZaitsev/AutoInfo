package com.baikaleg.v3.autoinfo.ui.stations.station

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import com.baikaleg.v3.autoinfo.data.model.Station

class StationItemViewModel(station: Station) : BaseObservable() {
    @Bindable
    private var data = ObservableField<Station>()

    init {
        data.set(station)
    }

    fun getData(): ObservableField<Station> {
        return data
    }
}