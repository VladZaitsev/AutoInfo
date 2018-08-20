package com.baikaleg.v3.autoinfo.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.Repository
import com.baikaleg.v3.autoinfo.data.db.Station
import com.baikaleg.v3.autoinfo.ui.main.MainActivity
import com.baikaleg.v3.autoinfo.ui.main.MainActivityModel
import com.google.android.gms.location.*


private const val NOTIFICATION_ID = 101
private const val INTERVAL: Long = 1000
private const val EARTH_RADIUS = 6378.1370

class StationSearchService : Service() {
    private val outerDistance = 70
    private val innerDistance = 30

    private var announcementType: Int? = 0
    //announcementType = 0 - no announcement about next station
    //announcementType = 1 - announce next station right after current station announcement
    //announcementType = 2 - announce next station right after current station is drove throw

    private var routeType = 0
    private var prevIndex = 0

    private var isOuter: Boolean = false
    private var isInner: Boolean = false
    private var isDirectionChanged: Boolean = false

    private var stations: List<Station>? = null

    private lateinit var repository: Repository

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        repository = Repository.getInstance(this.applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationCallback()
        createLocationRequest()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        announcementType = intent?.getIntExtra(MainActivityModel.STATION_ANNOUNCEMENT_TYPE_EXTRA, 0)

        val route: String? = intent?.getStringExtra(MainActivityModel.ROUTE_EXTRA)
        stations = repository.getStations(route)
        if (stations?.filter { station -> station.type == 2 }?.size != 0) {
            routeType = 2
        }

        startLocationUpdates()
        startForeground(NOTIFICATION_ID, setNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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
                    findNearestStation(location.latitude, location.longitude)
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
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

    private fun updateHandler(distance: Double?, index: Int, station: Station?) {
        if (distance!! < outerDistance) {
            if (!isOuter) {
                isOuter = true
                announceCurrentStation()

                if (index - prevIndex < 0) {
                    isDirectionChanged = true
                    prevIndex = 0
                    when (routeType) {
                        0 -> routeType = 1
                        1 -> routeType = 0
                        2 -> sendMessage(getString(R.string.msg_wrong_direction))
                    }
                } else {
                    isDirectionChanged = false
                    prevIndex = index
                }
                if (announcementType == 1 && !isDirectionChanged) announceNextStation(true)
            } else {
                if (announcementType == 2) {
                    if (distance < innerDistance) {
                        isInner = true
                    } else {
                        if (isInner) {
                            if (!isDirectionChanged) announceNextStation(false)
                            isInner = false
                        }
                    }
                }
            }
        } else {
            isOuter = false
        }
    }

    private fun findNearestStation(lat: Double, long: Double) {
        val list: List<Double>? = stations
                ?.filter { station -> station.type == routeType }
                ?.map { station -> getDistance(lat, long, station.latitude, station.longitude) }
        if (list != null && list.isNotEmpty()) {
            val distance = list.min()
            val index = list.indexOf(distance)
            val currentStation = stations?.filter { station -> station.type == routeType }?.get(index)
            updateHandler(distance, index, currentStation)
        }
    }

    private fun getDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val long = (long2 - long1) * (Math.PI / 180.0)
        val lat = (lat2 - lat1) * (Math.PI / 180.0)
        val a = Math.pow(Math.sin(lat / 2.0), 2.0) + (Math.cos(lat1 * (Math.PI / 180.0)) * Math.cos(lat2 * (Math.PI / 180.0))
                * Math.pow(Math.sin(long / 2.0), 2.0))
        val c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return EARTH_RADIUS * c
    }

    private fun announceNextStation(withDelay: Boolean) {

    }

    private fun announceCurrentStation() {

    }

    private fun sendMessage(msh: String) {

    }
}


