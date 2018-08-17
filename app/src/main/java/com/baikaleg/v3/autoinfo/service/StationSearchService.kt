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

class StationSearchService :
        Service() {

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
        val route: String? = intent?.getStringExtra(MainActivityModel.ROUTE_EXTRA)
        stations = repository.getStations(route)

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
                    // Update UI with location data
                    // ...
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
                .setContentText("AutoInfo is active")
        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }
}