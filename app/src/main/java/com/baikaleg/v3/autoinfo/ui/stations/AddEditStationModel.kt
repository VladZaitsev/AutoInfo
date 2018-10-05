package com.baikaleg.v3.autoinfo.ui.stations

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import com.baikaleg.v3.autoinfo.data.ANNOUNCE_AUDIO_TYPE_TTS
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.service.stationsearch.createLocationRequest
import com.google.android.gms.location.*
import org.json.JSONArray

class AddEditStationModel(application: Application, private val route: Route, private val navigator: OnStationChangeNavigator) : AndroidViewModel(application) {
    private val rawStations = MutableLiveData<List<Station>>()

    private lateinit var locationCallback: LocationCallback
    private var locationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest

    private val pref = QueryPreferences(application)
    private val repository = Repository.getInstance(application)

    private var isDirect = true

    val isCircleRoute = MutableLiveData<Boolean>()
    val stations = MutableLiveData<List<Station>>()
    val isTTS = MutableLiveData<Boolean>()
    val isLocationSearch = MutableLiveData<Boolean>()

    //fields
    val longitude = MutableLiveData<Double>()
    val latitude = MutableLiveData<Double>()
    val description = MutableLiveData<String>()

    init {
        isTTS.value = pref.getAnnounceAudioType() == ANNOUNCE_AUDIO_TYPE_TTS
        isCircleRoute.value = route.isCircle

        locationRequest = createLocationRequest(2000)
        locationClient = LocationServices.getFusedLocationProviderClient(application)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        latitude.value = location.latitude
                        longitude.value = location.longitude

                        isLocationSearch.value = false

                        locationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }

        rawStations.value = loadStationsList(application)//repository.getStations(route.name)
        load()
    }

    fun setStation(station: Station) {
        latitude.value = station.latitude
        longitude.value = station.longitude
        description.value = station.shortDescription
    }

    fun changeDirection() {
        load()
    }

    fun requestLocationPermission() {
        if (isLocationControlAllowed()) requestGpsSettings()
    }

    fun requestGpsSettings() {
        navigator.onLocationSettingsRequest(locationRequest)
    }

    @SuppressLint("MissingPermission")
    fun findLocation() {
        isLocationSearch.value = true
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun isLocationControlAllowed(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                navigator.onLocationPermissionRequest()
                return false
            }
        }
        return true
    }

    private fun load() {
        if (!route.isCircle) {
            stations.value = if (isDirect) {
                isDirect = false
                rawStations.value?.filter { station -> !station.isDirect }
            } else {
                isDirect = true
                rawStations.value?.filter { station -> station.isDirect }
            }
        }
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
                    jsonObject.getBoolean("type")
            )
//            station.voicePath = jsonObject.getString("voice_path")
            // station.id = jsonObject.getLong("id")
            station.orderNumber = i
            stations.add(station)
        }
        return stations
    }
}