package com.baikaleg.v3.autoinfo.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_ACCESS_FINE_LOCATION = 201

class MainActivity : AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        StationServiceNavigator {


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
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = MainActivityModel.create(this)
        viewModel.setNavigator(this)

        binding.viewmodel = viewModel
        binding.setLifecycleOwner(this)
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
