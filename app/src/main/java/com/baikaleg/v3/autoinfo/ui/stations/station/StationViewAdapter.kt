package com.baikaleg.v3.autoinfo.ui.stations.station

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.baikaleg.v3.autoinfo.data.model.Station
import com.baikaleg.v3.autoinfo.databinding.ItemViewStationBinding

class StationViewAdapter(private val navigator: StationClickNavigator) : RecyclerView.Adapter<StationViewAdapter.StationViewHolder>() {

    private var stations: MutableList<Station> = mutableListOf()

    override fun getItemCount(): Int {
        return stations.size
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val viewModel = StationItemViewModel(stations[position])
        holder.binding.viewmodel = viewModel

        holder.binding.root.setOnClickListener { _ -> navigator.onClick(stations[position]) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val binding = ItemViewStationBinding.inflate(LayoutInflater.from(parent.context))
        return StationViewHolder(binding)
    }

    fun refresh(list: List<Station>) {
        stations = list.toMutableList()
        notifyDataSetChanged()
    }

    inner class StationViewHolder(val binding: ItemViewStationBinding) : RecyclerView.ViewHolder(binding.root)

    interface StationClickNavigator {
        fun onClick(station: Station)
    }
}