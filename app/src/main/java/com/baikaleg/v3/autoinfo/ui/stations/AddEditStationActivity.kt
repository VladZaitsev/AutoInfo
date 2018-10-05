package com.baikaleg.v3.autoinfo.ui.stations

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.databinding.ActivityAddEditStationBinding
import com.baikaleg.v3.autoinfo.ui.stations.station.OnStationClickNavigator
import com.baikaleg.v3.autoinfo.ui.stations.station.StationViewAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_add_edit_station.*

@BindingAdapter("app:stations")
fun setStations(recyclerView: RecyclerView, stations: List<Station>?) {
    val adapter = recyclerView.adapter as StationViewAdapter
    stations?.let { adapter.refresh(it) }
}

private const val REQUEST_ACCESS_FINE_LOCATION = 201
private const val REQUEST_CHECK_SETTINGS = 202

const val ROUTE_EXTRA_DATA = "route_extra"

class AddEditStationActivity : AppCompatActivity(),
        OnStationClickNavigator,
        OnStationChangeNavigator,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var viewModel: AddEditStationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAddEditStationBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_add_edit_station)
        setSupportActionBar(binding.toolbar)
        val actionBar = this.supportActionBar as ActionBar

        val route = Route("test", false) //intent.getParcelableExtra<Route>(ROUTE_EXTRA_DATA)

        val modelFactory = AddEditStationModelFactory(application, route, this)
        viewModel = ViewModelProviders
                .of(this@AddEditStationActivity, modelFactory)
                .get(AddEditStationModel::class.java)

        with(actionBar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = null
        }

        with(binding) {
            setLifecycleOwner(this@AddEditStationActivity)
            viewmodel = viewModel

            with(recycler) {
                layoutManager = LinearLayoutManager(this@AddEditStationActivity)
                adapter = StationViewAdapter(this@AddEditStationActivity)
            }

            with(collapsingToolbar) {
                title = route.name
                setExpandedTitleColor(resources.getColor(android.R.color.transparent))
            }
        }
    }

    override fun onClick(station: Station) {
        appbar.setExpanded(true)
        viewModel.setStation(station)
    }

    override fun onLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
    }

    override fun onLocationSettingsRequest(locationRequest: LocationRequest) {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            viewModel.findLocation()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.findLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.requestGpsSettings()
            } else {
                Toast.makeText(this, R.string.msg_use_location_not_allowed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}