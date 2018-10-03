package com.baikaleg.v3.autoinfo.ui.stations

import android.arch.lifecycle.ViewModelProviders
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.databinding.ActivityAddEditStationBinding
import com.baikaleg.v3.autoinfo.ui.stations.station.StationViewAdapter

@BindingAdapter("app:stations")
fun setStations(recyclerView: RecyclerView, stations: List<Station>?) {
    val adapter = recyclerView.adapter as StationViewAdapter
    stations?.let { adapter.refresh(it) }
}

class AddEditStationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAddEditStationBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_add_edit_station)
        setSupportActionBar(binding.toolbar)
        val actionBar = this.supportActionBar as ActionBar

        with(actionBar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = null
        }

        with(binding) {
            setLifecycleOwner(this@AddEditStationActivity)
            viewmodel = ViewModelProviders.of(this@AddEditStationActivity).get(AddEditStationModel::class.java)

            with(recycler) {
                layoutManager = LinearLayoutManager(this@AddEditStationActivity)
                adapter = StationViewAdapter()
            }

            with(collapsingToolbar){
                title = "Nokia"
                setExpandedTitleColor(resources.getColor(android.R.color.transparent))
            }
        }
    }
}