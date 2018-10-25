package com.baikaleg.v3.autoinfo.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.databinding.ActivityMainBinding
import com.baikaleg.v3.autoinfo.service.stationsearch.StationSearchNavigator
import com.baikaleg.v3.autoinfo.service.stationsearch.createLocationRequest
import com.baikaleg.v3.autoinfo.ui.routes.RouteActivity
import com.baikaleg.v3.autoinfo.ui.settings.SettingsActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

private const val REQUEST_ACCESS_FINE_LOCATION = 201
private const val REQUEST_CHECK_SETTINGS = 202
private const val REQUEST_GOOGLE_PLAY_SERVICES = 203

class MainActivity : AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        StationSearchNavigator {


    private lateinit var viewModel: MainActivityModel

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.checkLocationSettings()
            } else {
                Toast.makeText(this, R.string.msg_use_location_not_allowed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.runService()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        viewModel = MainActivityModel.create(this)
        viewModel.setNavigator(this)

        binding.viewmodel = viewModel
        binding.setLifecycleOwner(this)

        val diameter = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            resources.displayMetrics.heightPixels * 0.65
        } else {
            resources.displayMetrics.widthPixels * 0.75
        }
        binding.container.stopGoBtn.layoutParams.height = diameter.toInt()
        binding.container.stopGoBtn.layoutParams.width = diameter.toInt()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.routes -> {
                val intent = Intent(this, RouteActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshUI()
    }

    override fun onLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
    }

    override fun onMessageReceived(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onLocationSettingsRequest() {
        val locationRequest = createLocationRequest(2000)
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            viewModel.runService()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    override fun isGooglePlayServicesAvailable(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        REQUEST_GOOGLE_PLAY_SERVICES).show()
            }
            return false
        }
        return true
    }
}
