package com.baikaleg.v3.autoinfo.ui.station

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.model.Station

private const val PARAM_ADD_STATION_DIALOG_ID = 0
private const val PARAM_EDIT_STATION_DIALOG_ID = 1

class AddEditStationModel(application: Application) : AndroidViewModel(application) {
    private val station = MutableLiveData<Station>()
    private val stations = MutableLiveData<List<Station>>()

    private var selected: Int = 0

    private val repository = Repository.getInstance(application)
    private var id = 0


    private fun saveStation() {
        station.value?.let {
            if (id == PARAM_ADD_STATION_DIALOG_ID) repository.saveStation(it)
            else if (id == PARAM_EDIT_STATION_DIALOG_ID) repository.updateStation(it)
        }
    }

}