package com.baikaleg.v3.autoinfo.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.databinding.ActivityMainBinding
import com.baikaleg.v3.autoinfo.service.stationsearch.StationSearchNavigator
import com.baikaleg.v3.autoinfo.ui.routes.RouteActivity
import com.baikaleg.v3.autoinfo.ui.settings.SettingsActivity

private const val REQUEST_ACCESS_FINE_LOCATION = 201

class MainActivity : AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        StationSearchNavigator {


    private lateinit var viewModel: MainActivityModel

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.startService()
            } else {
                Toast.makeText(this, R.string.msg_use_location_not_allowed, Toast.LENGTH_SHORT).show()
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.routes -> {
                val intent = Intent(this, RouteActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        viewModel.cancel()
        super.onDestroy()
    }

    override fun onLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
    }

    override fun onMessageReceived(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
