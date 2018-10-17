package com.baikaleg.v3.autoinfo.ui.stations

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.databinding.ActivityAddEditStationBinding
import com.baikaleg.v3.autoinfo.ui.stations.dialog.RecordStationVoiceDialog
import com.baikaleg.v3.autoinfo.ui.stations.station.StationTouchCallback
import com.baikaleg.v3.autoinfo.ui.stations.station.StationViewAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

@BindingAdapter("app:stations")
fun setStations(recyclerView: RecyclerView, stations: List<Station>?) {
    val adapter = recyclerView.adapter as StationViewAdapter
    if (stations != null) {
        adapter.refresh(stations)
    }
}

private const val REQUEST_ACCESS_FINE_LOCATION = 201
private const val REQUEST_ACCESS_RECORD_AUDIO = 202
private const val REQUEST_CHECK_SETTINGS = 203

private const val REQUEST_VOICE_CREATED = 305

const val ROUTE_EXTRA_DATA = "route_extra"

class AddEditStationActivity : AppCompatActivity(),
        StationViewAdapter.StationClickNavigator,
        OnStationModelStateCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var viewModel: AddEditStationModel
    private lateinit var route: Route
    private lateinit var stationAdapter: StationViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAddEditStationBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_add_edit_station)
        setSupportActionBar(binding.toolbar)
        val actionBar = this.supportActionBar as ActionBar

        route = intent.getParcelableExtra<Route>(ROUTE_EXTRA_DATA)
        stationAdapter = StationViewAdapter(this)
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
                adapter = stationAdapter
            }

            with(collapsingToolbar) {
                title = route.name
                setExpandedTitleColor(resources.getColor(android.R.color.transparent))
            }
        }
        binding.recycler.addItemDecoration(MarginItemDecoration(
                resources.getDimension(R.dimen.general_margin).toInt()))

        val callback = StationTouchCallback(this, viewModel)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recycler)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.station_menu, menu)
        val item = menu?.findItem(R.id.change_direction)
        item?.isVisible = !route.isCircle
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.change_direction) {
            viewModel.changeDirection()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(station: Station) {
        viewModel.setStation(station)
    }

    override fun onLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
    }

    override fun onRecordPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_ACCESS_RECORD_AUDIO)
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

    override fun onMessageReceived(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onRecordBtnClicked(desc: String) {
        RecordStationVoiceDialog.show(this, desc)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.findLocation()
            }
        } else if (requestCode == REQUEST_VOICE_CREATED) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.requestGpsSettings()
                } else {
                    Toast.makeText(this, R.string.msg_use_location_not_allowed, Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_ACCESS_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty()) {
                    val audioPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storagePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (audioPermission && storagePermission) {
                        viewModel.recordVoice()
                    } else if (!audioPermission) {
                        Toast.makeText(this, R.string.msg_audio_record_not_allowed, Toast.LENGTH_SHORT).show()
                    } else if (!storagePermission) {
                        Toast.makeText(this, R.string.msg_storage_writing_not_allowed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State) {
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    top = spaceHeight
                }
                left = spaceHeight
                right = spaceHeight
                bottom = spaceHeight
            }
        }
    }
}