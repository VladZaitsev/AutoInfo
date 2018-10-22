package com.baikaleg.v3.autoinfo.ui.stations

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.baikaleg.v3.autoinfo.audio.AudioController
import com.baikaleg.v3.autoinfo.audio.VOICE_PATH
import com.baikaleg.v3.autoinfo.data.ANNOUNCE_AUDIO_TYPE_TTS
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.exportimport.ExportImportRoute
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.service.stationsearch.createLocationRequest
import com.google.android.gms.location.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * The ViewModel for [AddEditStationActivity]
 */
class AddEditStationModel(application: Application, val route: Route, private val navigator: OnStationModelStateCallback) :
        AndroidViewModel(application) {

    private lateinit var locationCallback: LocationCallback
    private var locationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest

    private val pref = QueryPreferences(application)
    private val repository = Repository.getInstance(application)
    private val compositeDisposable = CompositeDisposable()
    private var isDirect = true

    private var allStations = mutableListOf<Station>()
    var stations = MutableLiveData<MutableList<Station>>()
    val isTTS = MutableLiveData<Boolean>()
    val isLocationSearch = MutableLiveData<Boolean>()
    val audio = AudioController(getApplication())

    //fields
    val longitude = MutableLiveData<String>()
    val latitude = MutableLiveData<String>()
    val shortDescription = MutableLiveData<String>()

    private val exportImportRoute = ExportImportRoute(application)

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
        compositeDisposable.add(repository.getRoute(route.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it: Route ->
                    allStations.clear()
                    allStations.addAll(it.stations)
                    stations.postValue(allStations.asSequence()
                            .filter { station -> station.isDirect == isDirect }
                            .sortedBy { station -> station.id }
                            .toMutableList())

                    latitude.value = ""
                    longitude.value = ""
                    shortDescription.value = ""
                })

        shortDescription.postValue("")
        latitude.postValue("")
        longitude.postValue("")
    }

    fun onMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition != toPosition) {
            stations.value!![fromPosition].id = toPosition
            if (fromPosition > toPosition) {
                for (i in toPosition until fromPosition) {
                    stations.value!![i].id = i + 1
                }
            } else {
                for (i in fromPosition + 1..toPosition) {
                    stations.value!![i].id = i - 1
                }
            }
            route.stations = stations.value!!
            repository.updateRoute(route)
        }
    }

    fun onRemoved(position: Int) {
        if (position != stations.value!!.size) {
            for (i in position until stations.value!!.size) {
                stations.value!![i].id = i - 1
            }
            stations.value!!.removeAt(position)
        }
        route.stations = stations.value!!
        repository.updateRoute(route)
    }

    fun setStation(station: Station) {
        latitude.value = station.latitude.toString()
        longitude.value = station.longitude.toString()
        shortDescription.value = station.shortDescription
    }

    fun changeDirection() {
        if (!route.isCircle) {
            isDirect = !isDirect
            stations.value = allStations.asSequence()
                    .filter { station -> station.isDirect == isDirect }
                    .sortedBy { station -> station.id }
                    .toMutableList()
        }
    }

    fun saveStation() {
        when {
            TextUtils.isEmpty(shortDescription.value) -> navigator.onMessageReceived("Please, type the description of the station")
            TextUtils.isEmpty(latitude.value) -> navigator.onMessageReceived("Please, type the latitude of the station")
            TextUtils.isEmpty(longitude.value) -> navigator.onMessageReceived("Please, type the longitude of the station")
            else -> {
                val station = Station(stations.value?.size!!, shortDescription.value!!, "", latitude.value!!.toDouble(), longitude.value!!.toDouble(), isDirect)
                if (stations.value?.contains(station)!!) {
                    navigator.onMessageReceived("You also have this station")
                } else {
                    allStations.add(station)
                    route.stations = allStations
                    repository.updateRoute(route)
                }
            }
        }
    }

    fun recordVoice() {
        if (TextUtils.isEmpty(shortDescription.value)) {
            navigator.onMessageReceived("Please, type the description of the station")
        } else {
            navigator.onRecordBtnClicked(shortDescription.value!!)
        }
    }

    fun playVoice() {
        if (TextUtils.isEmpty(shortDescription.value)) {
            navigator.onMessageReceived("Please, type the description of  the station")
        } else {
            if (isTTS.value!!) {
                audio.announceStation(shortDescription.value, 0)
            } else {
                val output = Environment.getExternalStorageDirectory().path + VOICE_PATH + shortDescription.value + ".3gp"
                val file = File(output)
                if (file.exists()) {
                    audio.announceStation(shortDescription.value, 0)
                } else {
                    navigator.onMessageReceived("You do not have record")
                }
            }
        }
    }

    fun selectRoute() {
        pref.setRoute(route.route)
    }

    fun exportRoute() {
        exportImportRoute.exportToFile(route)
    }

    fun requestLocationPermission() {
        if (isLocationControlAllowed()) requestGpsSettings()
    }

    fun requestAudioRecordPermission() {
        if (isRecordVoiceAllowed()) recordVoice()
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

    private fun isRecordVoiceAllowed(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                navigator.onRecordPermissionRequest()
                return false
            }
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        exportImportRoute.clear()
    }
}