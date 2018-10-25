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
import com.baikaleg.v3.autoinfo.R
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

//TODO Implement description

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
    private val audio = AudioController(getApplication())

    private var isDirect = true
    private var selectedStation: Station? = null
    private var allStations = mutableListOf<Station>()
    var stations = MutableLiveData<MutableList<Station>>()
    val isTTS = MutableLiveData<Boolean>()
    val isLocationSearch = MutableLiveData<Boolean>()
    val isStationNew = MutableLiveData<Boolean>()

    //fields
    val longitude = MutableLiveData<String>()
    val latitude = MutableLiveData<String>()
    val shortDescription = MutableLiveData<String>()

    private val exportImportRoute = ExportImportRoute(application)

    init {
        isTTS.value = pref.getAnnounceAudioType() == ANNOUNCE_AUDIO_TYPE_TTS
        isStationNew.value = true

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
                            .sortedBy { station -> station.num }
                            .toMutableList())
                    refresh()
                })
    }

    fun onMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition != toPosition) {
            var index = allStations.indexOf(stations.value!![fromPosition])
            allStations[index].num = toPosition + 1
            if (fromPosition > toPosition) {
                for (i in toPosition until fromPosition) {
                    index = allStations.indexOf(stations.value!![i])
                    allStations[index].num = allStations[index].num + 1
                }
            } else {
                for (i in fromPosition + 1..toPosition) {
                    index = allStations.indexOf(stations.value!![i])
                    allStations[index].num = allStations[index].num - 1
                }
            }
            route.stations = allStations
            repository.updateRoute(route)
        }
    }

    fun onRemoved(position: Int) {
        if (position != stations.value!!.size) {
            for (i in position until stations.value!!.size) {
                val index = allStations.indexOf(stations.value!![i])
                allStations[index].num = allStations[index].num - 1
            }
            val index = allStations.indexOf(stations.value!![position])
            allStations.removeAt(index)
        }
        route.stations = allStations
        repository.updateRoute(route)
    }

    fun setStation(station: Station) {
        selectedStation = station
        isStationNew.value = false
        latitude.value = station.latitude.toString()
        longitude.value = station.longitude.toString()
        shortDescription.value = station.shortDescription
    }

    fun changeDirection() {
        if (!route.isCircle) {
            isDirect = !isDirect
            stations.value = allStations.asSequence()
                    .filter { station -> station.isDirect == isDirect }
                    .sortedBy { station -> station.num }
                    .toMutableList()
        }
    }

    fun onSaveBtnClick() {
        when {
            TextUtils.isEmpty(shortDescription.value) -> navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_enter_description))
            TextUtils.isEmpty(latitude.value) -> navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_enter_latitude))
            TextUtils.isEmpty(longitude.value) -> navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_enter_longitude))
            else -> {
                if (isStationNew.value!!) {
                    val newStation = Station(stations.value?.size!! + 1, shortDescription.value!!, "", latitude.value!!.toDouble(), longitude.value!!.toDouble(), isDirect)
                    if (stations.value?.contains(newStation)!!) {
                        navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_you_also_have_this_station))
                    }
                    allStations.add(newStation)
                    saveStation()
                } else {
                    val index = allStations.indexOf(selectedStation)
                    selectedStation?.description = ""
                    selectedStation?.shortDescription = shortDescription.value!!
                    selectedStation?.latitude = latitude.value!!.toDouble()
                    selectedStation?.longitude = longitude.value!!.toDouble()
                    selectedStation?.isDirect = isDirect

                    navigator.onStationChangedRequest(index, selectedStation!!)
                }
            }
        }
    }

    fun onRequestLocationPermissionBtnClick() {
        if (isLocationControlAllowed()) requestGpsSettings()
    }

    fun onRequestAudioRecordPermissionBtnClick() {
        if (isRecordVoiceAllowed()) recordVoice()
    }

    fun onRefreshBtnClick() {
        refresh()
    }

    fun onPlayVoiceBtnClick() {
        if (TextUtils.isEmpty(shortDescription.value)) {
            navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_enter_description))
        } else {
            if (isTTS.value!!) {
                audio.announceStation(shortDescription.value, 0)
            } else {
                val output = Environment.getExternalStorageDirectory().path + VOICE_PATH + shortDescription.value + ".3gp"
                val file = File(output)
                if (file.exists()) {
                    audio.announceStation(shortDescription.value, 0)
                } else {
                    navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_you_donnot_have_record))
                }
            }
        }
    }

    fun changeStation(index: Int, station: Station) {
        allStations[index] = station
        saveStation()
    }

    fun recordVoice() {
        if (TextUtils.isEmpty(shortDescription.value)) {
            navigator.onMessageReceived(getApplication<Application>().getString(R.string.msg_enter_description))
        } else {
            navigator.onRecordBtnClicked(shortDescription.value!!)
        }
    }

    fun selectRoute() {
        pref.setRoute(route.route)
    }

    fun exportRoute() {
        exportImportRoute.exportToFile(route)
    }

    fun requestGpsSettings() {
        navigator.onLocationSettingsRequest(locationRequest)
    }

    @SuppressLint("MissingPermission")
    fun findLocation() {
        isLocationSearch.value = true
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    private fun refresh() {
        latitude.value = ""
        longitude.value = ""
        shortDescription.value = ""
        isStationNew.value = true
        selectedStation = null
    }

    private fun saveStation() {
        route.stations = allStations
        repository.updateRoute(route)
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