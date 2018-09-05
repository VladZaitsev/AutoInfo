package com.baikaleg.v3.autoinfo.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.db.Station
import com.baikaleg.v3.autoinfo.ui.main.*
import com.baikaleg.v3.autoinfo.utils.StationAudioSystem
import com.google.android.gms.location.*
import org.json.JSONObject

private const val NOTIFICATION_ID = 101
private const val INTERVAL: Long = 1000
private const val EARTH_RADIUS = 6378.1370


class StationSearchService : Service() {
    private val outerDistance = 0.07
    private val innerDistance = 0.03

    private var announcementType: Int? = 0
    //announcementType = 0 - no announcement about next station
    //announcementType = 1 - announce next station right after current station announcement
    //announcementType = 2 - announce next station right after current station is drove throw

    private var routeType = 0

    private var prevIndex = 0
    private var isOuter: Boolean = false
    private var isInner: Boolean = false
    private var isDirectionChanged: Boolean = false

    private lateinit var fullStationsList: List<Station>

    private lateinit var repository: Repository
    private lateinit var audioSystem: StationAudioSystem

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    interface OnStationStateChanged {
        fun announceCurrentStation(station: Station)
        fun announceNextStation(station: Station)
        fun isDirectionChanged(b: Boolean)
    }

    override fun onCreate() {
        super.onCreate()
        repository = Repository.getInstance(this.applicationContext)
        audioSystem = StationAudioSystem(this.applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationCallback()
        createLocationRequest()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        announcementType = intent?.getIntExtra(STATION_ANNOUNCEMENT_TYPE_EXTRA, 0)

        val route: String? = intent?.getStringExtra(ROUTE_EXTRA)
        fullStationsList = repository.getStations(route)!!

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
    }

    @SuppressWarnings("MissingPermission")
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    announceNearestStationsState(
                            location.latitude,
                            location.longitude,
                            fullStationsList,
                            object : OnStationStateChanged {
                                override fun announceCurrentStation(station: Station) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun announceNextStation(station: Station) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun announceNearestStationsState(lat: Double, long: Double, list: List<Station>, listener: OnStationStateChanged): JSONObject? {
        val stations = list.filter { station -> station.type == routeType }
        val distToStationList: List<Double>? = stations.map { station -> getDistance(lat, long, station.latitude, station.longitude) }
        if (distToStationList != null && stations.isNotEmpty()) {
            val distance = distToStationList.min()
            val index = distToStationList.indexOf(distance)
            if (distance!! < outerDistance) {
                if (!isOuter) {
                    isOuter = true
                    listener.announceCurrentStation(stations[index])
                    if (index - prevIndex < 0) {
                        listener.isDirectionChanged(true)
                        prevIndex = 0
                        when (routeType) {
                            0 -> routeType = 1
                            1 -> routeType = 0
                            2 -> sendMessage(getString(R.string.msg_wrong_direction))
                        }
                    } else {
                        if (stations.size != index + 1) {
                            displayDirection("${stations[prevIndex].short_description} - ${stations[index].short_description}")
                            listener.isDirectionChanged(false)
                            prevIndex = index
                        }
                    }
                    if (announcementType == 1 && !isDirectionChanged) {
                        listener.announceNextStation(stations[index + 1])
                    }
                } else {
                    if (announcementType == 2) {
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
        return null
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = INTERVAL
            fastestInterval = INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
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


