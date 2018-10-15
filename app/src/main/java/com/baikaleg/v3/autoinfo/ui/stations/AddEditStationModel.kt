package com.baikaleg.v3.autoinfo.ui.stations

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.pm.PackageManager
import android.databinding.Bindable
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.baikaleg.v3.autoinfo.audio.AudioController
import com.baikaleg.v3.autoinfo.audio.VOICE_PATH
import com.baikaleg.v3.autoinfo.data.ANNOUNCE_AUDIO_TYPE_PLAYER
import com.baikaleg.v3.autoinfo.data.ANNOUNCE_AUDIO_TYPE_TTS
import com.baikaleg.v3.autoinfo.data.QueryPreferences
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.service.stationsearch.createLocationRequest
import com.baikaleg.v3.autoinfo.ui.stations.station.StationTouchCallback
import com.google.android.gms.location.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * The ViewModel for [AddEditStationActivity]
 */
class AddEditStationModel(application: Application, val route: Route, private val navigator: OnStationModelStateCallback) :
        StationTouchCallback.ItemTouchHelperContract,
        AndroidViewModel(application) {

    private lateinit var locationCallback: LocationCallback
    private var locationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest

    private val pref = QueryPreferences(application)
    private val repository = Repository.getInstance(application)

    private var isDirect = true

    private var allStations = MutableLiveData<List<Station>>()
    var stations = MutableLiveData<MutableList<Station>>()
    val isTTS = MutableLiveData<Boolean>()
    val isLocationSearch = MutableLiveData<Boolean>()
    val audio = AudioController(getApplication())

    //fields
    val longitude = MutableLiveData<String>()
    val latitude = MutableLiveData<String>()
    val shortDescription = MutableLiveData<String>()

    init {
        //TODO change
        isTTS.value = true
        pref.setAnnounceAudioType(ANNOUNCE_AUDIO_TYPE_TTS)

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
                    stations.postValue(data?.filter { station -> station.isDirect == isDirect }?.toMutableList())
                })

        shortDescription.postValue("")
        latitude.postValue("")
        longitude.postValue("")
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition > toPosition) {
            val from = stations.value!![fromPosition]
            from.orderNumber = toPosition + 1

            stations.value!!.removeAt(fromPosition)
            for (i in toPosition + 1..stations.value!!.size) {
                val ss = stations.value!![i - 1]
                ss.orderNumber = i + 1
                repository.updateStation(ss)
            }
            repository.updateStation(from)
        } else {
            val from = stations.value!![fromPosition]
            from.orderNumber = toPosition + 1

            stations.value!!.removeAt(fromPosition)
            for (i in fromPosition until toPosition) {
                val ss = stations.value!![i]
                ss.orderNumber = i + 1
                repository.updateStation(ss)
            }
            repository.updateStation(from)
        }

    }

    override fun onRemoved(position: Int) {
        if (position != stations.value!!.size) {
            for (i in position + 1 until stations.value!!.size) {
                val s = stations.value!![i]
                s.orderNumber = i
                repository.updateStation(s)
            }
        }
        repository.deleteStation(stations.value!![position])
    }

    fun setStation(station: Station) {
        latitude.value = station.latitude.toString()
        longitude.value = station.longitude.toString()
        shortDescription.value = station.shortDescription
    }

    fun changeDirection() {
        if (!route.isCircle) {
            isDirect = !isDirect
            stations.value = allStations.value?.filter { station -> station.isDirect == isDirect }?.toMutableList()
        }
    }

    fun saveStation() {
        when {
            TextUtils.isEmpty(shortDescription.value) -> navigator.onMessageReceived("Please, type the description of the station")
            TextUtils.isEmpty(latitude.value) -> navigator.onMessageReceived("Please, type the latitude of the station")
            TextUtils.isEmpty(longitude.value) -> navigator.onMessageReceived("Please, type the longitude of the station")
            else -> {
                val station = Station(route.name, shortDescription.value!!, latitude.value!!.toDouble(), longitude.value!!.toDouble(), isDirect)
                station.orderNumber = stations.value?.size!! + 1
                if (stations.value?.contains(station)!!) {
                    navigator.onMessageReceived("You also have this station")
                } else {
                    stations.value?.add(station)
                    repository.saveStation(station)
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

    fun uploadVoice() {

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