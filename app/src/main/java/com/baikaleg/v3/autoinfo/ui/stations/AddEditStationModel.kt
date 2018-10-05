package com.baikaleg.v3.autoinfo.ui.stations

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * The ViewModel for [AddEditStationActivity]
 */
class AddEditStationModel(application: Application, val route: Route, private val navigator: OnStationChangeNavigator) : AndroidViewModel(application) {

    private lateinit var locationCallback: LocationCallback
    private var locationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest

    private val pref = QueryPreferences(application)
    private val repository = Repository.getInstance(application)

    private var isDirect = true

    var allStations = MutableLiveData<List<Station>>()
    var stations = MutableLiveData<List<Station>>()
    val isTTS = MutableLiveData<Boolean>()
    val isLocationSearch = MutableLiveData<Boolean>()

    //fields
    val longitude = MutableLiveData<String>()
    val latitude = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    init {
        isTTS.value = pref.getAnnounceAudioType() == ANNOUNCE_AUDIO_TYPE_TTS

        locationRequest = createLocationRequest(2000)
        locationClient = LocationServices.getFusedLocationProviderClient(application)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        latitude.value = location.latitude.toString()
                        longitude.value = location.longitude.toString()

                        isLocationSearch.value = false

                        locationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }
        repository.getStations(route.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data: List<Station>? ->
                    allStations.postValue(data)
                    stations.postValue(data?.filter { station -> station.isDirect == isDirect })
                })
    }

    fun setStation(station: Station) {
        latitude.value = station.latitude.toString()
        longitude.value = station.longitude.toString()
        description.value = station.shortDescription
    }

    fun changeDirection() {
        if(!route.isCircle){
            isDirect = !isDirect
            stations.value = allStations.value?.filter { station -> station.isDirect == isDirect }
        }
    }

    fun saveStation() {
        val station = Station(route.name, description.value!!, latitude.value!!.toDouble(), longitude.value!!.toDouble(), isDirect)
        repository.saveStation(station)
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
}