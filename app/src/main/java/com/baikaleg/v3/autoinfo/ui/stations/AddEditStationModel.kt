package com.baikaleg.v3.autoinfo.ui.stations

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.model.Station
import org.json.JSONArray

private const val PARAM_ADD_STATION_DIALOG_ID = 0
private const val PARAM_EDIT_STATION_DIALOG_ID = 1

class AddEditStationModel(application: Application) : AndroidViewModel(application) {
    //  private val station = MutableLiveData<Station>()
    val stations = MutableLiveData<List<Station>>()

    private var selected: Int = 0

    private val repository = Repository.getInstance(application)
    private var id = 0


    init {
        load(application)
    }

    /* private fun saveStation() {
         station.value?.let {
             if (id == PARAM_ADD_STATION_DIALOG_ID) repository.saveStation(it)
             else if (id == PARAM_EDIT_STATION_DIALOG_ID) repository.updateStation(it)
         }
     }*/

    private fun load(application: Application) {
        stations.value = loadStationsList(application)
    }

    private fun loadStationsList(context: Context): List<Station> {
        val stations: MutableList<Station> = mutableListOf()
        val jsonString = context.assets.open("test_stations.txt").bufferedReader().use {
            it.readText()
        }
        val array = JSONArray(jsonString)
        for (i in 0 until (array.length())) {
            val jsonObject = array.getJSONObject(i)
            val station = Station(
                    jsonObject.getString("route"),
                    jsonObject.getString("short_description"),
                    jsonObject.getDouble("latitude"),
                    jsonObject.getDouble("longitude"),
                    jsonObject.getInt("type")
            )
//            station.voicePath = jsonObject.getString("voice_path")
            station.id = jsonObject.getLong("id")
            station.orderNumber = i
            stations.add(station)
        }
        return stations
    }
}