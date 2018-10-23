package com.baikaleg.v3.autoinfo.service.stationsearch

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.ui.main.*
import com.baikaleg.v3.autoinfo.audio.AudioController
import com.baikaleg.v3.autoinfo.data.*
import com.baikaleg.v3.autoinfo.data.model.Route
import com.google.android.gms.location.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun createLocationRequest(time: Long): LocationRequest {
    return LocationRequest().apply {
        interval = time
        fastestInterval = time
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}

private const val NOTIFICATION_ID = 101
private const val INTERVAL: Long = 1000
private const val EARTH_RADIUS = 6378.1370

class StationSearchService : Service() {
    private val outerDistance = 0.07
    private val innerDistance = 0.03

    private var announcementType: Int = 0
    private var isDirect = true

    private var prevIndex = 0
    private var isOuter: Boolean = false
    private var isInner: Boolean = false
    private var isDirectionChanged: Boolean = false

    private lateinit var fullStationsList: List<Station>

    private lateinit var repository: Repository
    private lateinit var audioSystem: AudioController

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val compositeDisposable = CompositeDisposable()

    interface OnStationStateChanged {
        fun announceCurrentStation(station: Station)
        fun announceNextStation(station: Station)
        fun isDirectionChanged(b: Boolean)
    }

    override fun onCreate() {
        super.onCreate()
        repository = Repository.getInstance(this.applicationContext)
        audioSystem = AudioController(this.applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val pref = QueryPreferences(this)
        announcementType = pref.getAnnounceStationType()

        createLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val pref = QueryPreferences(getApplication())
        compositeDisposable.add(repository.getRoute(pref.getRoute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data: Route -> fullStationsList = data.stations })

        startLocationUpdates()

        startForeground(NOTIFICATION_ID, setNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return StationSearchBinder()
    }

    inner class StationSearchBinder : Binder() {
        fun getService(): StationSearchService {
            return this@StationSearchService
        }
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
        compositeDisposable.clear()
        audioSystem.cancel()
    }

    @SuppressWarnings("MissingPermission")
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    announceNearestStation(
                            location.latitude,
                            location.longitude,
                            fullStationsList,
                            object : OnStationStateChanged {
                                override fun announceCurrentStation(station: Station) {
                                    if (announcementType == ANNOUNCE_STATION_TYPE_EMPTY)
                                        audioSystem.announceStation(station.shortDescription, 0)
                                    else if (announcementType == ANNOUNCE_STATION_TYPE_CURRENT) {
                                        audioSystem.announceStation(station.shortDescription, 1)
                                    }
                                }

                                override fun announceNextStation(station: Station) {
                                    audioSystem.announceStation(station.shortDescription, 2)
                                }

                                override fun isDirectionChanged(b: Boolean) {
                                    isDirectionChanged = b
                                }
                            }
                    )
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(createLocationRequest(INTERVAL), locationCallback, null)
    }

    fun announceNearestStation(lat: Double, long: Double, list: List<Station>, listener: OnStationStateChanged) {
        val stations = list.filter { station -> station.isDirect == isDirect }
        val distToStationList: List<Double>? = stations.map { station -> getDistance(lat, long, station.latitude, station.longitude) }
        if (distToStationList != null && stations.isNotEmpty()) {
            val distance = distToStationList.min()
            val index = distToStationList.indexOf(distance)
            if (distance!! < outerDistance) {
                if (!isOuter) {
                    if (index != 0 && index - prevIndex <= 0) {
                        listener.isDirectionChanged(true)
                        isDirectionChanged = true
                        prevIndex = 0
                        isDirect = !isDirect
                        return
                    } else {
                        isOuter = true
                        listener.announceCurrentStation(stations[index])
                        if (stations.size != index + 1) {
                            displayDirection("${stations[prevIndex].shortDescription} - ${stations[index].shortDescription}")
                            listener.isDirectionChanged(false)
                            isDirectionChanged = false
                            prevIndex = index
                            if (announcementType == ANNOUNCE_STATION_TYPE_NEXT) {
                                Thread.sleep(2000)
                                listener.announceNextStation(stations[index + 1])
                            }
                        }
                    }
                } else {
                    if (announcementType == ANNOUNCE_STATION_TYPE_NEXT_WITH_DELAY) {
                        if (distance < innerDistance) {
                            isInner = true
                        } else {
                            if (isInner) {
                                if (!isDirectionChanged && stations.size != index + 1) {
                                    listener.announceNextStation(stations[index + 1])
                                }
                                isInner = false
                            }
                        }
                    }
                }
            } else {
                isOuter = false
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun setNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this)
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.btn_stop)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notif_content))
        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }

    private fun getDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val long = (long2 - long1) * (Math.PI / 180.0)
        val lat = (lat2 - lat1) * (Math.PI / 180.0)
        val a = Math.pow(Math.sin(lat / 2.0), 2.0) + (Math.cos(lat1 * (Math.PI / 180.0)) * Math.cos(lat2 * (Math.PI / 180.0))
                * Math.pow(Math.sin(long / 2.0), 2.0))
        val c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return EARTH_RADIUS * c
    }

    private fun displayStationDescription(stg: String) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(PARAM_STATUS, STATUS_STATION)
        intent.putExtra(PARAM_STATION, stg)
        sendBroadcast(intent)
    }

    private fun displayDirection(dir: String) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(PARAM_STATUS, STATUS_DIRECTION)
        intent.putExtra(PARAM_DIRECTION, dir)
        sendBroadcast(intent)
    }

    private fun sendMessage(msg: String) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(PARAM_STATUS, STATUS_MSG)
        intent.putExtra(PARAM_MSG, msg)
        sendBroadcast(intent)
    }
}


