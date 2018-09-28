package com.baikaleg.v3.autoinfo.ui.station

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.baikaleg.v3.autoinfo.R
import kotlinx.android.synthetic.main.activity_main.*


class AddEditStationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_station)
        setSupportActionBar(toolbar)
    }
}